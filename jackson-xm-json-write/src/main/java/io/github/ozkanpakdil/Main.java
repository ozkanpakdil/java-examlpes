package io.github.ozkanpakdil;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        json();
        xml();
    }

    private static void xml() throws Exception {
        List<Item> values = Arrays.asList(new Item("hello"), new Item("world"));

        XmlFactory factory = new XmlFactory();
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        factory.setCodec(xmlMapper);
        ToXmlGenerator generator = factory.createGenerator(new File("test,xml"), JsonEncoding.UTF8);

        generator.writeObject(values);
        generator.close();
    }

    private static void json() throws IOException {
        List<Item> values = Arrays.asList(new Item("hello"), new Item("world"));
        JsonFactory factory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        factory.setCodec(objectMapper);
        JsonGenerator generator = factory.createGenerator(new File("test,json"), JsonEncoding.UTF8);
        generator.writeObject(values);
        generator.close();
    }
}