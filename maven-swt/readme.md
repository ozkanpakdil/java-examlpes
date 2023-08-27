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