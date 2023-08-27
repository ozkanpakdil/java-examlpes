# how to compile and start
```
mvn package
```
for starting from command line
```
mvn exec:java
```

in case you want to build for windows use below dependency
```
<dependency>
    <groupId>org.eclipse.platform</groupId>
    <artifactId>org.eclipse.swt.win32.win32.x86_64</artifactId>
    <version>3.124.0</version>
</dependency>
```

for graalvm build
```
mvn -Pnative -Dagent=true -DskipTests package exec:exec@native
```

getting error below
```
The build process encountered an unexpected error:

> com.oracle.svm.core.util.VMError$HostedError: com.oracle.svm.core.util.UserError$UserException: Image heap writing found a class not seen during static analysis. Did a static field or an object referenced from a static field change during native image generation? For example, a lazily initialized cache could have been initialized during image generation, in which case you need to force eager initialization of the cache before static analysis or reset the cache using a field value recomputation.
    class: sun.security.x509.X509CertImpl
```