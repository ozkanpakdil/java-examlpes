#include <jni.h>
#include <stdio.h>
#include "HelloWorld.h"

JNIEXPORT void JNICALL
Java_HelloWorld_print(JNIEnv *env, jobject obj) {
	char name[10];
    printf("Please enter your name. \n");
    scanf("%s", name);
    printf("Hello from C %s",name);
}
