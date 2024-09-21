#include <jni.h>
#include <stdio.h>

JNIEXPORT void JNICALL
Java_HelloWorld_print(JNIEnv *env, jobject obj) {
	char name[1];
    printf("Please enter your name. \n");
    scanf("%s", name);
    printf("Hello from C %s",name);
}
