# how to run

mvn compile exec:java -Dexec.mainClass="com.ozkan.TestJS"

## requires graalvm installed

```shell
sdk install java 21-graalce
```

### example output from my local

```shell
oz-mint@ozmint-MACH-WX9:~/projects/java-examlpes/graal-js$ mvn compile exec:java -Dexec.mainClass="com.ozkan.TestJS"
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------------< com.ozkan:graal-js >-------------------------
[INFO] Building demo 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ graal-js ---
[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory /home/oz-mint/projects/java-examlpes/graal-js/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ graal-js ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Compiling 1 source file to /home/oz-mint/projects/java-examlpes/graal-js/target/classes
[WARNING] /home/oz-mint/projects/java-examlpes/graal-js/src/main/java/com/ozkan/TestJS.java: /home/oz-mint/projects/java-examlpes/graal-js/src/main/java/com/ozkan/TestJS.java uses unchecked or unsafe operations.
[WARNING] /home/oz-mint/projects/java-examlpes/graal-js/src/main/java/com/ozkan/TestJS.java: Recompile with -Xlint:unchecked for details.
[INFO] 
[INFO] --- exec-maven-plugin:3.1.0:java (default-cli) @ graal-js ---
Graal.js 23.1.0 [js, JS, JavaScript, javascript, ECMAScript, ecmascript, Graal.js, graal.js, Graal-js, graal-js, Graal.JS, Graal-JS, GraalJS, GraalJSPolyglot]
[To redirect Truffle log output to a file use one of the following options:
* '--log.file=<path>' if the option is passed using a guest language launcher.
* '-Dpolyglot.log.file=<path>' if the option is passed using the host Java launcher.
* Configure logging using the polyglot embedding API.]
[engine] WARNING: The polyglot context is using an implementation that does not support runtime compilation.
The guest application code will therefore be executed in interpreted mode only.
Execution only in interpreted mode will strongly impact the guest application performance.
For more information on using GraalVM see https://www.graalvm.org/java/quickstart/.
To disable this warning the '--engine.WarnInterpreterOnly=false' option or use the '-Dpolyglot.engine.WarnInterpreterOnly=false' system property.
hey I am in js.
{"c":"c","f":"f"}
{}
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.389 s
[INFO] Finished at: 2023-09-23T18:43:58+01:00
[INFO] ------------------------------------------------------------------------
oz-mint@ozmint-MACH-WX9:~/projects/java-examlpes/graal-js$ 

```