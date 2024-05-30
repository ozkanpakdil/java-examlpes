package com.oracle.example.graalpy;

import org.graalvm.polyglot.*;

import java.io.File;
import java.io.IOException;


/**
 * A basic embedded GraalPy Hello World application.
 */
public class Main {

public static void main(String[] args) throws IOException {
    try (
            Engine engine = Engine.newBuilder()
                    .option("engine.WarnInterpreterOnly", "false")
                    .build();
            Context context = Context
                    .newBuilder("python")
                    .engine(engine)
                    .build()) {
        File file = new File("Hello.py");
        Source source = Source.newBuilder("python", file).build();
        context.eval(source);
    } catch (PolyglotException e) {
        e.printStackTrace();
        SourceSection s = e.getSourceLocation();
        int line = s.getStartLine();
        int col = s.getStartColumn();
        System.out.println("line:" + line + " col:" + col + " Err:" + e.getMessage());
    }
}
}
