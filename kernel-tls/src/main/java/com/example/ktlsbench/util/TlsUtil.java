package com.example.ktlsbench.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public final class TlsUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private TlsUtil() {
    }

    public static SSLContext createJsseServerContext(boolean tls13) throws Exception {
        KeyPair keyPair = generateKeyPair();
        X509Certificate cert = selfSigned("CN=JsseServer", keyPair);
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("key", keyPair.getPrivate(), new char[0], new Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);
        SSLContext ctx = SSLContext.getInstance(tls13 ? "TLSv1.3" : "TLSv1.2");
        ctx.init(kmf.getKeyManagers(), new TrustManager[]{insecureTrustAllManager()}, new SecureRandom());
        return ctx;
    }

    public static SSLContext createJsseClientContext(boolean tls13) throws Exception {
        SSLContext ctx = SSLContext.getInstance(tls13 ? "TLSv1.3" : "TLSv1.2");
        ctx.init(null, new TrustManager[]{insecureTrustAllManager()}, new SecureRandom());
        return ctx;
    }

    public static X509TrustManager insecureTrustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    private static X509Certificate selfSigned(String dn, KeyPair keyPair) throws Exception {
        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 1000L * 60);
        Date notAfter = new Date(now + 3650L * 24 * 60 * 60 * 1000); // ~10 years
        BigInteger serial = new BigInteger(64, new SecureRandom());

        X500Name subject = new X500Name(dn);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject, serial, notBefore, notAfter, subject, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509CertificateHolder holder = builder.build(signer);
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }
}
