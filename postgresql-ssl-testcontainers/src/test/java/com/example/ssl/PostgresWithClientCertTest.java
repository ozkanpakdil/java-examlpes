package com.example.ssl;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.MountableFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.example.ssl.CryptoUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test class for PostgreSQL with SSL and client certificate authentication.
 * This test demonstrates secure PostgreSQL connections using SSL/TLS with client certificate verification.
 * <p>
 * Prerequisites:
 * - Docker must be available and running on the test machine
 * - Test will be automatically skipped if Docker is not available
 * - Test timeout is set to 60 seconds
 * <p>
 * Test Features:
 * - Sets up a PostgreSQL container with SSL enabled
 * - Configures client certificate authentication (verify-full mode)
 * - Demonstrates JDBC connection with SSL verification
 * - Tests psql CLI connection (if available)
 * <p>
 * Security Configuration:
 * - Generates runtime CA and certificates using {@link CryptoUtil}
 * - Creates server certificate for "localhost"
 * - Creates client certificate for test user authentication
 * - Configures PostgreSQL with custom pg_hba.conf requiring client certificates
 *
 * @see CryptoUtil For certificate generation utilities
 * @see org.testcontainers.containers.PostgreSQLContainer
 */
@Timeout(60)
public class PostgresWithClientCertTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresWithClientCertTest.class);

    static PostgreSQLContainer<?> pg;
    static Path tempDir;

    static Path caCertPem;
    static Path serverCertPem;
    static Path serverKeyPem;

    static Path clientCertPem;
    static Path clientKeyPem;

    static Path clientKeystore;
    static Path truststore;

    static final String TEST_USER = "testuser";
    static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    static boolean dockerAvailable;

    @BeforeAll
    static void setup() throws Exception {
        // Fast pre-check: ensure Docker is available to avoid long Testcontainers hangs
        dockerAvailable = isDockerAvailable();
        if (!dockerAvailable) {
            // Do not start Testcontainers; individual tests will be marked as skipped via assumptions in @BeforeEach
            return;
        }

        tempDir = Files.createTempDirectory("pgssl-");

        // 1) Generate CA, server, client certs
        var ca = generateSelfSignedCA("Demo-CA");
        var server = issueCertificate(ca, "localhost", true, List.of("localhost"));
        var client = issueCertificate(ca, TEST_USER, false, List.of());

        caCertPem = tempDir.resolve("root.crt");
        serverCertPem = tempDir.resolve("server.crt");
        serverKeyPem = tempDir.resolve("server.key");
        clientCertPem = tempDir.resolve("client.crt");
        clientKeyPem = tempDir.resolve("client.key");

        writePemCertificate(caCertPem, ca.certificate());
        writePemCertificate(serverCertPem, server.certificate());
        writePemPrivateKey(serverKeyPem, server.keyPair().getPrivate());
        writePemCertificate(clientCertPem, client.certificate());
        // Write client private key in PKCS#8 for PgJDBC LibPQFactory compatibility
        writePemPrivateKeyPkcs8(clientKeyPem, client.keyPair().getPrivate());
        // Ensure client.key has strict permissions for psql
        try {
            PosixFileAttributeView view = Files.getFileAttributeView(clientKeyPem, PosixFileAttributeView.class);
            if (view != null) {
                Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(clientKeyPem, perms);
            } else {
                clientKeyPem.toFile().setReadable(true, true);
                clientKeyPem.toFile().setWritable(true, true);
                clientKeyPem.toFile().setExecutable(false, true);
            }
        } catch (Exception ignore) { /* best-effort for non-POSIX FS */ }

        // 2) Create Java keystore and truststore for JDBC
        clientKeystore = tempDir.resolve("client.p12");
        truststore = tempDir.resolve("truststore.p12");
        createPkcs12KeyStore(clientKeystore, "client", client.keyPair().getPrivate(), new Certificate[]{client.certificate(), ca.certificate()}, KEYSTORE_PASSWORD);
        createPkcs12TrustStore(truststore, "ca", ca.certificate(), KEYSTORE_PASSWORD);

        // 3) Create pg_hba.conf that requires client cert
        Path pgHba = tempDir.resolve("pg_hba.conf");
        String hba = """
                # TYPE  DATABASE        USER            ADDRESS                 METHOD
                
                # Allow local socket connections for init scripts and superuser tasks
                local   all            all                                     scram-sha-256
                host    all            all             127.0.0.1/32            scram-sha-256
                host    all            all             ::1/128                 scram-sha-256
                
                # Require client certs for all TCP connections from anywhere
                hostssl all            all             0.0.0.0/0               cert clientcert=verify-full
                hostssl all            all             ::0/0                   cert clientcert=verify-full
                """;
        Files.writeString(pgHba, hba);

        // 4) Init SQL to create role matching client CN
        Path initSql = tempDir.resolve("init.sql");
        String sql = "CREATE ROLE \"" + TEST_USER + "\" LOGIN;";
        Files.writeString(initSql, sql);

        // 5) Create init shell script to move certs into $PGDATA with proper permissions and enable SSL
        Path sslInit = tempDir.resolve("00-ssl.sh");
        String script = """
                #!/bin/bash
                set -euo pipefail
                echo '[ssl-init] Configuring Postgres SSL (copying files into PGDATA)'
                
                # Copy SSL materials from init directory into $PGDATA
                cp /docker-entrypoint-initdb.d/server.crt "$PGDATA"/server.crt
                cp /docker-entrypoint-initdb.d/server.key "$PGDATA"/server.key
                cp /docker-entrypoint-initdb.d/root.crt "$PGDATA"/root.crt
                
                # Ensure strict perms on keys and certs
                chmod 600 "$PGDATA"/server.key || true
                chmod 644 "$PGDATA"/server.crt "$PGDATA"/root.crt || true
                
                # Enable SSL and point to the files (relative to $PGDATA)
                echo "ssl=on" >> "$PGDATA"/postgresql.conf
                echo "ssl_cert_file='server.crt'" >> "$PGDATA"/postgresql.conf
                echo "ssl_key_file='server.key'" >> "$PGDATA"/postgresql.conf
                echo "ssl_ca_file='root.crt'" >> "$PGDATA"/postgresql.conf
                echo "listen_addresses='*'" >> "$PGDATA"/postgresql.conf
                
                # Replace pg_hba.conf to require client certs
                cp /docker-entrypoint-initdb.d/pg_hba.conf "$PGDATA"/pg_hba.conf
                echo '[ssl-init] Done'
                """;
        Files.writeString(sslInit, script);
        sslInit.toFile().setExecutable(true);

        // 6) Start Postgres with default entrypoint; copy files into /docker-entrypoint-initdb.d
        pg = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("postgres")
                .withStartupTimeout(Duration.ofSeconds(30)).withLogConsumer(new Slf4jLogConsumer(LOG).withSeparateOutputStreams())
                // Provide init scripts and SSL assets
                .withCopyFileToContainer(MountableFile.forHostPath(initSql), "/docker-entrypoint-initdb.d/01-init.sql")
                .withCopyFileToContainer(MountableFile.forHostPath(pgHba), "/docker-entrypoint-initdb.d/pg_hba.conf")
                // Copy SSL files into init directory; 00-ssl.sh will move them into $PGDATA during init
                .withCopyFileToContainer(MountableFile.forHostPath(serverCertPem, 0644), "/docker-entrypoint-initdb.d/server.crt")
                // server.key must be readable by the 'postgres' user during init scripts; tighten perms in $PGDATA later
                .withCopyFileToContainer(MountableFile.forHostPath(serverKeyPem, 0644), "/docker-entrypoint-initdb.d/server.key")
                .withCopyFileToContainer(MountableFile.forHostPath(caCertPem, 0644), "/docker-entrypoint-initdb.d/root.crt")
                .withCopyFileToContainer(MountableFile.forHostPath(sslInit, 0755), "/docker-entrypoint-initdb.d/00-ssl.sh");

        try {
            pg.start();
        } catch (Exception e) {
            System.out.println("[DEBUG_LOG] Container STDOUT/STDERR logs:\n" + pg.getLogs());
            throw e;
        }
    }

    @BeforeEach
    void assumeDocker() {
        Assumptions
                .assumeTrue(dockerAvailable, "Docker is not available or not responding within timeout; " +
                        "skipping Testcontainers-based tests");
    }

    @AfterAll
    static void tearDown() {
        if (pg != null) pg.stop();
        // tempDir left for inspection on failures
    }

    @Test
    @DisplayName("JDBC verify-full with client certificate")
    void jdbcVerifyFullWithClientCert() throws Exception {
        String url = "jdbc:postgresql://localhost:" + pg.getFirstMappedPort() + "/postgres";

        // Configure JSSE to use our keystore/truststore (PKCS12)
        System.setProperty("javax.net.ssl.keyStore", clientKeystore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.keyStorePassword", new String(KEYSTORE_PASSWORD));
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.trustStore", truststore.toAbsolutePath().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", new String(KEYSTORE_PASSWORD));
        System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");

        Properties props = new Properties();
        props.setProperty("user", TEST_USER);
        props.setProperty("ssl", "true");
        props.setProperty("sslmode", "verify-full");
        props.setProperty("sslfactory", "org.postgresql.ssl.DefaultJavaSSLFactory");
        // hostname 'localhost' must match server cert SAN

        try (Connection conn = DriverManager.getConnection(url, props); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("select current_user, (select ssl from pg_stat_ssl where pid = pg_backend_pid()) as ssl_used");
            assertTrue(rs.next());
            String user = rs.getString(1);
            boolean sslUsed = rs.getBoolean(2);
            assertEquals(TEST_USER, user);
            assertTrue(sslUsed, "SSL should be active for this connection");
        }
    }

    @Test
    @DisplayName("psql (if installed) can connect with client certificate and verify-full")
    void psqlCliIfAvailable() throws Exception {
        if (!isPsqlAvailable()) {
            Assumptions.abort("psql not available on PATH; skipping CLI test");
            return;
        }
        int port = pg.getFirstMappedPort();
        String conninfo = String.join(" ", "host=localhost", "port=" + port, "dbname=postgres", "user=" + TEST_USER, "sslmode=verify-full", "sslrootcert=" + caCertPem.toAbsolutePath(), "sslcert=" + clientCertPem.toAbsolutePath(), "sslkey=" + clientKeyPem.toAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder("psql", "--set", "ON_ERROR_STOP=on", "-c", "select 1;");
        pb.environment().put("PGSERVICEFILE", "");
        pb.environment().put("PGCONNECT_TIMEOUT", "10");
        pb.environment().put("PGTZ", "UTC");
        pb.environment().put("PGSSLMODE", "verify-full");
        pb.environment().put("PGHOST", "localhost");
        pb.environment().put("PGPORT", String.valueOf(port));
        pb.environment().put("PGDATABASE", "postgres");
        pb.environment().put("PGUSER", TEST_USER);
        pb.environment().put("PGSSLROOTCERT", caCertPem.toAbsolutePath().toString());
        pb.environment().put("PGSSLCERT", clientCertPem.toAbsolutePath().toString());
        pb.environment().put("PGSSLKEY", clientKeyPem.toAbsolutePath().toString());

        Process p = pb.start();
        String out = readAll(p.getInputStream());
        String err = readAll(p.getErrorStream());
        boolean finished = p.waitFor(20, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            fail("psql did not finish in time\nSTDOUT:\n" + out + "\nSTDERR:\n" + err);
        }
        int code = p.exitValue();
        assertEquals(0, code, "psql failed. stdout:\n" + out + "\nstderr:\n" + err);
    }

    private static boolean isPsqlAvailable() {
        try {
            Process p = new ProcessBuilder("psql", "--version").start();
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            return finished && p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private static String readAll(InputStream in) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private static boolean isDockerAvailable() {
        try {
            // Run through bash so user-defined aliases/functions in ~/.bashrc are honored.
            // Using an interactive shell (-i) enables alias expansion; -c executes the command.
            Process p = new ProcessBuilder("bash", "-ic", "docker info").redirectErrorStream(true).start();
            boolean finished = p.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            LOG.warn("Failed to check Docker availability", e);
            return false;
        }
    }
}
