# removed the -c option because we need to link this
HelloWorld:
	javac -h . HelloWorld.java
	javac HelloWorld.java
	gcc -shared -Wall -Werror -IC:\sdkman\candidates\java\current\include -IC:\sdkman\candidates\java\current\include\win32 -o HelloWorld.dll -fPIC HelloWorld.c
	java -Djava.library.path=. HelloWorld
clean:
ifeq ($(OS),Windows_NT)
	del /Q *.o *.class
else
	rm -f *.o
endif