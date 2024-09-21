# how to compile the hellow world C code
`make` should be enough to build the dll, which will generate `HelloWorld.dll`

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