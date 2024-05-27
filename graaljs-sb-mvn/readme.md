## how to run
`mvn test` example output below
```shell
C:\Users\ozkan\projects\graaljs-sb-mvn> mvn test
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< org.springframework.boot:GraalVMTest >----------------
[INFO] Building GraalVMTest 3.2.5
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ GraalVMTest ---
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] Copying 0 resource from src\main\resources to target\classes
[INFO]
[INFO] --- compiler:3.11.0:compile (default-compile) @ GraalVMTest ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- resources:3.3.1:testResources (default-testResources) @ GraalVMTest ---
[INFO] skip non existing resourceDirectory C:\Users\ozkan\projects\graaljs-sb-mvn\src\test\resources
[INFO]
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ GraalVMTest ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- surefire:3.1.2:test (default-test) @ GraalVMTest ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.ozkanpakdil.graaljssbmvn.EngineTest
21:20:21.374 [main] INFO org.springframework.test.context.support.AnnotationConfigContextLoaderUtils -- Could not detect default configuration classes for test class [io.github.ozkanpakdil.graaljssbmvn.EngineTest]: EngineTest does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
21:20:21.821 [main] INFO org.springframework.boot.test.context.SpringBootTestContextBootstrapper -- Found @SpringBootConfiguration io.github.ozkanpakdil.graaljssbmvn.GraaljsSbMvnApplication for test class io.github.ozkanpakdil.graaljssbmvn.EngineTest

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)

2024-05-27T21:20:23.867+01:00  INFO 28060 --- [graaljs-sb-mvn] [           main] i.g.ozkanpakdil.graaljssbmvn.EngineTest  : Starting EngineTest using Java 21.0.1 with PID 28060 (started by ozkan in C:\Users\ozkan\projects\graaljs-sb-mvn)
2024-05-27T21:20:23.873+01:00  INFO 28060 --- [graaljs-sb-mvn] [           main] i.g.ozkanpakdil.graaljssbmvn.EngineTest  : No active profile set, falling back to 1 default profile: "default"
2024-05-27T21:20:25.791+01:00  INFO 28060 --- [graaljs-sb-mvn] [           main] i.g.ozkanpakdil.graaljssbmvn.EngineTest  : Started EngineTest in 3.245 seconds (process running for 7.854)
WARNING: A Java agent has been loaded dynamically (C:\Users\ozkan\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.13\byte-buddy-agent-1.14.13.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Graal.js-Development Build-[js, JS, JavaScript, javascript, ECMAScript, ecmascript, Graal.js, graal.js, Graal-js, graal-js, Graal.JS, Graal-JS, GraalJS, GraalJSPolyglot]
[ERROR] Tests run: 9, Failures: 5, Errors: 0, Skipped: 0, Time elapsed: 10.83 s <<< FAILURE! -- in io.github.ozkanpakdil.graaljssbmvn.EngineTest
[ERROR] io.github.ozkanpakdil.graaljssbmvn.EngineTest.test2 -- Time elapsed: 0.027 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: not <null>
        at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:152)
        at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
        at org.junit.jupiter.api.AssertNotNull.failNull(AssertNotNull.java:49)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:35)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:30)
        at org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:304)
        at io.github.ozkanpakdil.graaljssbmvn.EngineTest.test2(EngineTest.java:35)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[ERROR] io.github.ozkanpakdil.graaljssbmvn.EngineTest.test5 -- Time elapsed: 0.017 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: not <null>
        at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:152)
        at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
        at org.junit.jupiter.api.AssertNotNull.failNull(AssertNotNull.java:49)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:35)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:30)
        at org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:304)
        at io.github.ozkanpakdil.graaljssbmvn.EngineTest.test5(EngineTest.java:50)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[ERROR] io.github.ozkanpakdil.graaljssbmvn.EngineTest.test6 -- Time elapsed: 0.013 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: not <null>
        at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:152)
        at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
        at org.junit.jupiter.api.AssertNotNull.failNull(AssertNotNull.java:49)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:35)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:30)
        at org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:304)
        at io.github.ozkanpakdil.graaljssbmvn.EngineTest.test6(EngineTest.java:55)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[ERROR] io.github.ozkanpakdil.graaljssbmvn.EngineTest.test7 -- Time elapsed: 0.013 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: not <null>
        at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:152)
        at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
        at org.junit.jupiter.api.AssertNotNull.failNull(AssertNotNull.java:49)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:35)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:30)
        at org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:304)
        at io.github.ozkanpakdil.graaljssbmvn.EngineTest.test7(EngineTest.java:60)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[ERROR] io.github.ozkanpakdil.graaljssbmvn.EngineTest.test8 -- Time elapsed: 0.012 s <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: not <null>
        at org.junit.jupiter.api.AssertionFailureBuilder.build(AssertionFailureBuilder.java:152)
        at org.junit.jupiter.api.AssertionFailureBuilder.buildAndThrow(AssertionFailureBuilder.java:132)
        at org.junit.jupiter.api.AssertNotNull.failNull(AssertNotNull.java:49)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:35)
        at org.junit.jupiter.api.AssertNotNull.assertNotNull(AssertNotNull.java:30)
        at org.junit.jupiter.api.Assertions.assertNotNull(Assertions.java:304)
        at io.github.ozkanpakdil.graaljssbmvn.EngineTest.test8(EngineTest.java:65)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
        at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)

[INFO] Running io.github.ozkanpakdil.graaljssbmvn.GraaljsSbMvnApplicationTests
2024-05-27T21:20:31.153+01:00  INFO 28060 --- [graaljs-sb-mvn] [           main] t.c.s.AnnotationConfigContextLoaderUtils : Could not detect default configuration classes for test class [io.github.ozkanpakdil.graaljssbmvn.GraaljsSbMvnApplicationTests]: GraaljsSbMvnApplicationTests does not declare any static, non-private, non-final, nested classes annotated with @Configuration.
2024-05-27T21:20:31.160+01:00  INFO 28060 --- [graaljs-sb-mvn] [           main] .b.t.c.SpringBootTestContextBootstrapper : Found @SpringBootConfiguration io.github.ozkanpakdil.graaljssbmvn.GraaljsSbMvnApplication for test class io.github.ozkanpakdil.graaljssbmvn.GraaljsSbMvnApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.051 s -- in io.github.ozkanpakdil.graaljssbmvn.GraaljsSbMvnApplicationTests
[INFO] 
[INFO] Results:
[INFO]
[ERROR] Failures: 
[ERROR]   EngineTest.test2:35 expected: not <null>
[ERROR]   EngineTest.test5:50 expected: not <null>
[ERROR]   EngineTest.test6:55 expected: not <null>
[ERROR]   EngineTest.test7:60 expected: not <null>
[ERROR]   EngineTest.test8:65 expected: not <null>
[INFO]
[ERROR] Tests run: 10, Failures: 5, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  22.919 s
[INFO] Finished at: 2024-05-27T21:20:32+01:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.1.2:test (default-test) on project GraalVMTest: There are test failures.
[ERROR]
[ERROR] Please refer to C:\Users\ozkan\projects\graaljs-sb-mvn\target\surefire-reports for the individual test results.
[ERROR] Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
```
below names should have work but some of them are not working.
js, JS, JavaScript, javascript, ECMAScript, ecmascript, Graal.js, graal.js, Graal-js, graal-js, Graal.JS, Graal-JS, GraalJS, GraalJSPolyglot