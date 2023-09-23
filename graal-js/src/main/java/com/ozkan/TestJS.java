package com.ozkan;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.List;

public class TestJS {
    public static void main(String[] args) throws Exception {

        ScriptEngineManager sem = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = sem.getEngineFactories();
        for (ScriptEngineFactory factory : factories)
            System.out.println(factory.getEngineName() + " " + factory.getEngineVersion() + " " + factory.getNames());
        if (factories.isEmpty())
            System.out.println("No Script Engines found");

        HashMap<String, String> obj = new HashMap();
        obj.put("f", "f");
        obj.put("c", "c");

        ObjectMapper objectMapper = new ObjectMapper();

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("graal.js");

        engine.put("obj", obj);
        engine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE).put ("obj", obj);
        engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put ("obj", obj);

        System.out.println(engine.eval("""
                        console.log('hey I am in js.');
                        o=%s;
                        JSON.stringify(o);
                """.formatted(objectMapper.writeValueAsString(obj))));
        System.out.println(engine.eval("JSON.stringify(obj);"));
    }
}
