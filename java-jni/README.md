# how to compile the hello world C code
`make` should be enough to build the dll, which will generate `HelloWorld.dll` then it will run the commands too

# how to create HelloWorld.h
```
javac -h . HelloWorld.java
```

# how to run the java code
first compile `javac HelloWorld.java` then how to run `java -Djava.library.path=. HelloWorld` exampl e output below


```shell
ozkan@HP-ENVY-2021-I7 C:\Users\ozkan\tmp\java-jni
$ java -Djava.library.path=. HelloWorld
Please enter your name.
aaaa
Hello from C aaaa
```
chck the [c code](./HellowWorld.c)