package io.github.ozkanpakdil.graaljssbmvn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

@SpringBootApplication
public class GraaljsSbMvnApplication {

    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        for (ScriptEngineFactory factory : factories)
            System.out.println(factory.getEngineName() + "-" + factory.getEngineVersion() + "-" + factory.getNames());


        SpringApplication.run(GraaljsSbMvnApplication.class, args);
    }

}
