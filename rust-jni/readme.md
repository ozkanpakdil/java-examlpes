```
cd mylib
cargo build
cd ..
javac -h . HelloWorld.java
java "-Djava.library.path=./mylib/target/debug" HelloWorld
```

Example output
```shell
PS C:\Users\ozkan\projects\rust-jni> java "-Djava.library.path=./mylib/target/debug" HelloWorld
Hello from rust, Özkan!
```