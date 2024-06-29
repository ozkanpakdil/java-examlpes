# how to use jpackage
[jpackage](https://docs.oracle.com/en/java/javase/22/jpackage/packaging-overview.html) - tool for packaging self-contained Java applications.
```shell
mvn package
jpackage -i .\target\ -n hello --main-class com.example.demo.DemoApplication --main-jar demo-0.0.1-SNAPSHOT.jar
```

Reference:https://stackoverflow.com/a/78674853/175554