import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;

public class FFMSinTest {
    public static void main(String[] args) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        // Locate the "sin" function in the C math library
        MemorySegment sinAddress = stdlib.find("sin").orElseThrow();
        FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE);
        var sinHandle = linker.downcallHandle(sinAddress, descriptor);

        double angle = Math.PI / 4;  // 45 degrees in radians

        // Timing Java's Math.sin()
        long javaStartTime = System.nanoTime();
        double result = 0;
        for (int i = 0; i < 1_000_000; i++) {
            result = Math.sin(angle);
        }
        System.out.println(result);
        long javaEndTime = System.nanoTime();
        long javaDuration = javaEndTime - javaStartTime;

        // Timing C sin via FFM
        long ffmStartTime = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            result = (double) sinHandle.invoke(angle);
        }
        System.out.println(result);
        long ffmEndTime = System.nanoTime();
        long ffmDuration = ffmEndTime - ffmStartTime;

        System.out.println("Java Math.sin() took: " + javaDuration / 1_000_000.0 + " ms");
        System.out.println("C sin (FFM) took: " + ffmDuration / 1_000_000.0 + " ms");
    }
}
