# build
`mvn verify`
# run
`java -cp target/ffm-api-demo-1.0-SNAPSHOT.jar FFMSinTest`

# example run log
```shell
ozkan@HP-ENVY-2021-I7 C:\Users\ozkan\projects\java-examlpes\ffm
$ java -cp target/ffm-api-demo-1.0-SNAPSHOT.jar FFMSinTest
WARNING: A restricted method in java.lang.foreign.Linker has been called
WARNING: java.lang.foreign.Linker::downcallHandle has been called by FFMSinTest in an unnamed module
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled

Java Math.sin() took: 4.8677 ms
C sin (FFM) took: 78.9172 ms
```