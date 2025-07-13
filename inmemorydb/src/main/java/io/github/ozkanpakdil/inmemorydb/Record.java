package io.github.ozkanpakdil.inmemorydb;

import java.util.*;
import java.util.stream.Collectors;

public class Record {
    private final Map<String, Field> fields;

    public Record() {
        this.fields = new HashMap<>();
    }

    public void setField(String fieldName, String value) {
        fields.put(fieldName, new Field(value));
    }

    public void setFieldAt(String fieldName, String value, long timestamp) {
        fields.put(fieldName, new Field(value, timestamp));
    }

    public void setFieldWithTTL(String fieldName, String value, long timestamp, long ttl) {
        fields.put(fieldName, new Field(value, timestamp, ttl));
    }

    public String getField(String fieldName) {
        Field field = fields.get(fieldName);
        return field != null ? field.getValue() : "";
    }

    public String getFieldAt(String fieldName, long timestamp) {
        Field field = fields.get(fieldName);
        return (field != null && field.isExpired(timestamp)) ? field.getValue() : "";
    }

    public boolean deleteField(String fieldName) {
        return fields.remove(fieldName) != null;
    }

    public boolean deleteFieldAt(String fieldName, long timestamp) {
        Field field = fields.get(fieldName);
        if (field != null && field.isExpired(timestamp)) {
            fields.remove(fieldName);
            return true;
        }
        return false;
    }

    public String scan() {
        return fields.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s(%s)", e.getKey(), e.getValue().getValue()))
                .collect(Collectors.joining(", "));
    }

    public String scanAt(long timestamp) {
        return fields.entrySet().stream()
                .filter(e -> e.getValue().isExpired(timestamp))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s(%s)", e.getKey(), e.getValue().getValue()))
                .collect(Collectors.joining(", "));
    }

    public String scanByPrefix(String prefix) {
        return fields.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s(%s)", e.getKey(), e.getValue().getValue()))
                .collect(Collectors.joining(", "));
    }

    public String scanByPrefixAt(String prefix, long timestamp) {
        return fields.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix) && e.getValue().isExpired(timestamp))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s(%s)", e.getKey(), e.getValue().getValue()))
                .collect(Collectors.joining(", "));
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public Record createBackup(long timestamp) {
        Record backup = new Record();
        fields.entrySet().stream()
                .filter(e -> e.getValue().isExpired(timestamp))
                .forEach(e -> backup.fields.put(e.getKey(), e.getValue().copy()));
        return backup;
    }

    public Map<String, Field> getFieldsWithTTL() {
        return new HashMap<>(fields);
    }
}
