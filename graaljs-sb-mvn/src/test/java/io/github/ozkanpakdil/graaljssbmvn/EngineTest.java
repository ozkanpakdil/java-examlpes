package io.github.ozkanpakdil.graaljssbmvn;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class EngineTest {

    @Test
    void factoryTest() {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        for (ScriptEngineFactory factory : factories)
            System.out.println(factory.getEngineName() + "-" + factory.getEngineVersion() + "-" + factory.getNames());
        assertFalse(manager.getEngineFactories().isEmpty());
    }

    @Test
    void test1() {
        assertNotNull(getScriptEngineByName("js"));
    }

    @Test
    void test2() {
        assertNotNull(getScriptEngineByName("Javascript"));
    }

    @Test
    void test3() {
        assertNotNull(getScriptEngineByName("JavaScript"));
    }

    @Test
    void test4() {
        assertNotNull(getScriptEngineByName("graal.js"));
    }

    @Test
    void test5() {
        assertNotNull(getScriptEngineByExtension("JS"));
    }

    @Test
    void test6() {
        assertNotNull(getScriptEngineByExtension("Javascript"));
    }

    @Test
    void test7() {
        assertNotNull(getScriptEngineByExtension("JavaScript"));
    }

    @Test
    void test8() {
        assertNotNull(getScriptEngineByExtension("graal.js"));
    }


    private ScriptEngine getScriptEngineByName(String name) {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName(name);
    }

    private ScriptEngine getScriptEngineByExtension(String extension) {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByExtension(extension);
    }
}