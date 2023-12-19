1. oz-mint@ozmint-MACH-WX9:~/tmp/jvmti-examples/GetAllStackTraces/Native$ cmake CMakeLists.txt 
2. run `make` in that native fodler which will create the so file
3. `javac -g GetAllStackTracesTest.java` in the other folder
4. java -agentpath:../../Native/libGetAllStackTraces.so -agentlib:GetAllStackTraces GetAllStackTracesTest 
for more verbose logs below
java -verbose:jvmti -agentpath:../../Native/libGetAllStackTraces.so -agentlib:GetAllStackTraces GetAllStackTracesTest 

# jvmti-examples

A collection of JVMTI code examples covering the following -


## Methods

* Get All StackTraces
* Get System Properties



## Events (Still to be developed)

* VMInit
* VMDeath
* ThreadStart
* ThreadEnd;
* ClassFileLoadHook
* ClassLoad
* ClassPrepare
* VMStart
* Exception
* ExceptionCatch;
* SingleStep;
* FramePop
* Breakpoint
* FieldAccess
* FieldModification
* MethodEntry
* MethodExit
* NativeMethodBind
* CompiledMethodLoad
* CompiledMethodUnload
* DynamicCodeGenerated
* DataDumpRequest
* MonitorWait
* MonitorWaited
* MonitorContendedEnter
* MonitorContendedEntered
* ResourceExhausted
* GarbageCollectionStart
* GarbageCollectionFinish
* ObjectFree
* VMObjectAlloc



