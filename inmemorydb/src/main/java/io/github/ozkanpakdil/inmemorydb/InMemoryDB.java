package io.github.ozkanpakdil.inmemorydb;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDB {
    private final Map<String, Record> database;
    private final Map<Long, DatabaseBackup> backups;
    private long latestTimestamp;

    public InMemoryDB() {
        this.database = new ConcurrentHashMap<>();
        this.backups = new TreeMap<>();
        this.latestTimestamp = 0;
    }

    public String set(String key, String field, String value) {
        Record record = database.computeIfAbsent(key, _ -> new Record());
        record.setField(field, value);
        return "";
    }

    public String setAt(String key, String field, String value, long timestamp) {
        validateTimestamp(timestamp);
        Record record = database.computeIfAbsent(key, _ -> new Record());
        record.setFieldAt(field, value, timestamp);
        return "";
    }

    public String setAtWithTTL(String key, String field, String value, long timestamp, long ttl) {
        validateTimestamp(timestamp);
        Record record = database.computeIfAbsent(key, _ -> new Record());
        record.setFieldWithTTL(field, value, timestamp, ttl);
        return "";
    }

    public String get(String key, String field) {
        Record record = database.get(key);
        if (record == null) return "";
        return record.getField(field);
    }

    public String getAt(String key, String field, long timestamp) {
        validateTimestamp(timestamp);
        Record record = database.get(key);
        if (record == null) return "";
        return record.getFieldAt(field, timestamp);
    }

    public String delete(String key, String field) {
        Record record = database.get(key);
        if (record == null) return "false";
        return record.deleteField(field) ? "true" : "false";
    }

    public String deleteAt(String key, String field, long timestamp) {
        validateTimestamp(timestamp);
        Record record = database.get(key);
        if (record == null) return "false";
        return record.deleteFieldAt(field, timestamp) ? "true" : "false";
    }

    public String scan(String key) {
        Record record = database.get(key);
        if (record == null) return "";
        return record.scan();
    }

    public String scanAt(String key, long timestamp) {
        validateTimestamp(timestamp);
        Record record = database.get(key);
        if (record == null) return "";
        return record.scanAt(timestamp);
    }

    public String scanByPrefix(String key, String prefix) {
        Record record = database.get(key);
        if (record == null) return "";
        return record.scanByPrefix(prefix);
    }

    public String scanByPrefixAt(String key, String prefix, long timestamp) {
        validateTimestamp(timestamp);
        Record record = database.get(key);
        if (record == null) return "";
        return record.scanByPrefixAt(prefix, timestamp);
    }

    public String backup(long timestamp) {
        validateTimestamp(timestamp);
        DatabaseBackup backup = new DatabaseBackup(timestamp);
        for (Map.Entry<String, Record> entry : database.entrySet()) {
            Record recordCopy = entry.getValue().createBackup(timestamp);
            if (!recordCopy.isEmpty()) {
                backup.addRecord(entry.getKey(), recordCopy);
            }
        }
        backups.put(timestamp, backup);
        return String.valueOf(backup.getRecordCount());
    }

    public String restore(long timestamp, long timestampToRestore) {
        validateTimestamp(timestamp);
        Optional<Map.Entry<Long, DatabaseBackup>> backupEntry = backups.entrySet().stream()
                .filter(entry -> entry.getKey() <= timestampToRestore)
                .max(Map.Entry.comparingByKey());
        
        if (backupEntry.isPresent()) {
            database.clear();
            DatabaseBackup backup = backupEntry.get().getValue();
            backup.restore(database, timestamp);
        }
        return "";
    }

    private void validateTimestamp(long timestamp) {
        if (timestamp < latestTimestamp) {
            throw new IllegalArgumentException("Time cannot flow backwards");
        }
        latestTimestamp = timestamp;
    }
}
