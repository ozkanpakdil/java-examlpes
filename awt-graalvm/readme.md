# how to compile
used java version 20.0.2, run commands below first to generate the reflection jsons
```shell
mvn clean package
mvn -Pnative -Dagent exec:exec@java-agent
```
just use the calculator click around and do one calculation. Now for building
```shell
mvn -Pnative -Dagent package
```

Right now I am getting the error below. looks like awt is not supported by graalvm. https://github.com/oracle/graal/issues/2545
```shell
oz-mint@ozmint-MACH-WX9:~/tmp/swing/awt-graalvm$ ./target/demo 
Exception in thread "main" java.lang.Error: java.home property not set
	at java.desktop@20.0.2/sun.awt.FontConfiguration.findFontConfigFile(FontConfiguration.java:180)

```
I tried to set java.home but it did not help.