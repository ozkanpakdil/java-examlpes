## example useage

```shell
ozkan@hp-envy-2021-i7-nvidia:/mnt/c/Users/ozkan/tmp/java-securerandom$ java -version
openjdk version "23.0.1" 2024-10-15
OpenJDK Runtime Environment GraalVM CE 23.0.1+11.1 (build 23.0.1+11-jvmci-b01)
OpenJDK 64-Bit Server VM GraalVM CE 23.0.1+11.1 (build 23.0.1+11-jvmci-b01, mixed mode, sharing)
ozkan@hp-envy-2021-i7-nvidia:/mnt/c/Users/ozkan/tmp/java-securerandom$ java -jar target/java-securerandom-1.0-SNAPSHOT.jar 
Provider: SUN
Version: 23.0
Info: SUN (DSA key/parameter generation; DSA signing; SHA-1, MD5 digests; SecureRandom; X.509 certificates; PKCS12, JKS & DKS keystores; PKIX CertPathValidator; PKIX CertPathBuilder; LDAP, Collection CertStores, JavaPolicy Policy; JavaLoginConfig Configuration)
Provider: SunRsaSign
Version: 23.0
Info: Sun RSA signature provider
Provider: SunEC
Version: 23.0
Info: Sun Elliptic Curve provider
Provider: SunJSSE
Version: 23.0
Info: Sun JSSE provider(PKCS12, SunX509/PKIX key/trust factories, SSLv3/TLSv1/TLSv1.1/TLSv1.2/TLSv1.3/DTLSv1.0/DTLSv1.2)
Provider: SunJCE
Version: 23.0
Info: SunJCE Provider (implements RSA, DES, Triple DES, AES, Blowfish, ARCFOUR, RC2, PBE, Diffie-Hellman, HMAC, ChaCha20)
Provider: SunJGSS
Version: 23.0
Info: Sun (Kerberos v5, SPNEGO)
Provider: SunSASL
Version: 23.0
Info: Sun SASL provider(implements client mechanisms for: DIGEST-MD5, EXTERNAL, PLAIN, CRAM-MD5, NTLM; server mechanisms for: DIGEST-MD5, CRAM-MD5, NTLM)
Provider: XMLDSig
Version: 23.0
Info: XMLDSig (DOM XMLSignatureFactory; DOM KeyInfoFactory; C14N 1.0, C14N 1.1, Exclusive C14N, Base64, Enveloped, XPath, XPath2, XSLT TransformServices)
Provider: SunPCSC
Version: 23.0
Info: Sun PC/SC provider
Provider: JdkLDAP
Version: 23.0
Info: JdkLDAP Provider (implements LDAP CertStore)
Provider: JdkSASL
Version: 23.0
Info: JDK SASL provider(implements client and server mechanisms for GSSAPI)
Provider: SunPKCS11
Version: 23.0
Info: Unconfigured and unusable PKCS11 provider
Generating secure int: -1426253471
Generating secure long: -8658506601016642796
Generating secure float: 0.8077143
Generating secure double: 0.4531116322967683
Generating secure gaussian: -1.529973895112763
Generating secure bytes: [26, 5, 107, -33, -70, 9, 108, -116, 41, -104, 127, -20, 36, -104, -1, 122]
Generating random int with upper bound: 446
Value: 6
Value: 4
Value: 5
```