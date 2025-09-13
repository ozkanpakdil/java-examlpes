This project demonstrates how to prepare Testcontainers with PostgreSQL configured for SSL and client certificate authentication.

What it shows
- How to launch a PostgreSQL 16 container with SSL enabled.
- How to require client certificate authentication (verify-full) via pg_hba.conf.
- Two connection demos implemented as JUnit 5 tests:
  1) JDBC connection using sslmode=verify-full with a client certificate.
  2) Optional psql CLI connection (runs only if psql is available on your PATH).

How it works
- Tests generate a self-signed CA at runtime, then issue:
  - A server certificate for the hostname "localhost" (so verify-full passes when connecting via localhost).
  - A client certificate for user "testuser" (CN matches the DB role).
- The certs are copied into the PostgreSQL container before startup and SSL is enabled by passing -c flags to the server.
- A custom pg_hba.conf is provided that requires client certificates with verify-full.
- An init SQL script creates the role testuser for certificate authentication.
- For JDBC, the test builds a PKCS#12 keystore/truststore used by JSSE + PgJDBC with sslmode=verify-full.
- For psql, the test uses the PEM files via PGSSL* environment variables.

Prerequisites
- Docker available to the Testcontainers runtime.
- Java 17+ and Maven 3.9+.
- Optional: psql client installed if you want the CLI test to run.

Run the tests
- mvn -q -DskipTests=false test

Notes
- The psql test is conditional; it is skipped automatically if psql is not found.
- Certificates are generated into a temporary directory per test run.
- Server certificate SAN includes DNS:localhost so verify-full succeeds when connecting to localhost.
- Timeouts: tests have a class-level @Timeout(60s); container startup timeout is 30s; the build also enforces a 180s forked test-process timeout to avoid indefinite hangs.
- Docker availability: tests quickly self-skip when Docker is not reachable within ~5s (to avoid long Testcontainers detection delays). When Docker is unavailable, JUnit will report the tests as skipped rather than running zero tests.
- Maven/Guice Unsafe warning: you may see a warning about sun.misc.Unsafe from Maven’s internal Guice. It is unrelated to this project and can be safely ignored.
