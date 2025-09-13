package com.example.ssl;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Simple crypto utility to generate a CA and issue server/client certificates.
 */
public class CryptoUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public record CertBundle(KeyPair keyPair, X509Certificate certificate) {
    }

    public static KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    public static CertBundle generateSelfSignedCA(String commonName) throws Exception {
        KeyPair caKey = generateKeyPair();
        X500Name issuer = new X500Name("CN=" + commonName);

        Instant now = Instant.now();
        Date notBefore = Date.from(now.minusSeconds(60));
        Date notAfter = Date.from(now.plusSeconds(365 * 24 * 3600L));

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                issuer,
                BigInteger.valueOf(now.toEpochMilli()),
                notBefore, notAfter,
                issuer,
                caKey.getPublic()
        );
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(caKey.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        cert.checkValidity(new Date());
        cert.verify(caKey.getPublic());
        return new CertBundle(caKey, cert);
    }

    public static CertBundle issueCertificate(CertBundle ca, String commonName, boolean isServer, List<String> dnsSANs) throws Exception {
        KeyPair kp = generateKeyPair();
        X500Name subject = new X500Name("CN=" + commonName);
        X500Name issuer = new X500Name(ca.certificate().getSubjectX500Principal().getName());

        Instant now = Instant.now();
        Date notBefore = Date.from(now.minusSeconds(60));
        Date notAfter = Date.from(now.plusSeconds(180 * 24 * 3600L));

        SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(kp.getPublic().getEncoded());
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                issuer,
                BigInteger.valueOf(now.toEpochMilli() + (isServer ? 1 : 2)),
                notBefore, notAfter,
                subject,
                spki
        );

        // Extended Key Usage
        KeyPurposeId usage = isServer ? KeyPurposeId.id_kp_serverAuth : KeyPurposeId.id_kp_clientAuth;
        builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(new KeyPurposeId[]{usage}));
        builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));

        if (dnsSANs != null && !dnsSANs.isEmpty()) {
            GeneralName[] names = dnsSANs.stream()
                    .map(s -> new GeneralName(GeneralName.dNSName, s))
                    .toArray(GeneralName[]::new);
            GeneralNames san = new GeneralNames(names);
            builder.addExtension(Extension.subjectAlternativeName, false, san);
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(ca.keyPair().getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        cert.checkValidity(new Date());
        cert.verify(ca.certificate().getPublicKey());
        return new CertBundle(kp, cert);
    }

    public static void writePemPrivateKey(Path path, PrivateKey key) throws Exception {
        try (JcaPEMWriter writer = new JcaPEMWriter(new OutputStreamWriter(Files.newOutputStream(path)))) {
            if ("RSA".equalsIgnoreCase(key.getAlgorithm())) {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                java.security.spec.RSAPrivateCrtKeySpec spec = kf.getKeySpec(key, java.security.spec.RSAPrivateCrtKeySpec.class);
                org.bouncycastle.asn1.pkcs.RSAPrivateKey rsa = new org.bouncycastle.asn1.pkcs.RSAPrivateKey(
                        spec.getModulus(),
                        spec.getPublicExponent(),
                        spec.getPrivateExponent(),
                        spec.getPrimeP(),
                        spec.getPrimeQ(),
                        spec.getPrimeExponentP(),
                        spec.getPrimeExponentQ(),
                        spec.getCrtCoefficient());
                org.bouncycastle.util.io.pem.PemObject pem = new org.bouncycastle.util.io.pem.PemObject("RSA PRIVATE KEY", rsa.getEncoded());
                writer.writeObject(pem); // PKCS#1, BEGIN RSA PRIVATE KEY
            } else {
                writer.writeObject(key); // Fallback to PKCS#8
            }
        }
    }

    /**
     * Write the private key in PKCS#8 (BEGIN PRIVATE KEY) format, which PgJDBC LibPQFactory expects.
     */
    public static void writePemPrivateKeyPkcs8(Path path, PrivateKey key) throws Exception {
        try (JcaPEMWriter writer = new JcaPEMWriter(new OutputStreamWriter(Files.newOutputStream(path)))) {
            writer.writeObject(key); // PKCS#8, BEGIN PRIVATE KEY
        }
    }

    public static void writePemCertificate(Path path, Certificate cert) throws IOException {
        try (JcaPEMWriter writer = new JcaPEMWriter(new OutputStreamWriter(Files.newOutputStream(path)))) {
            writer.writeObject(cert);
        }
    }

    public static void createPkcs12KeyStore(Path path, String alias, Key key, Certificate[] chain, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(alias, key, password, chain);
        try (var out = Files.newOutputStream(path)) {
            ks.store(out, password);
        }
    }

    public static void createPkcs12TrustStore(Path path, String alias, Certificate cert, char[] password) throws Exception {
        KeyStore ts = KeyStore.getInstance("PKCS12");
        ts.load(null, null);
        ts.setCertificateEntry(alias, cert);
        try (var out = Files.newOutputStream(path)) {
            ts.store(out, password);
        }
    }
}
