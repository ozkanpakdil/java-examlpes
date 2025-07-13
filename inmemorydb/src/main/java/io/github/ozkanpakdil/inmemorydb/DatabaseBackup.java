package io.github.ozkanpakdil.inmemorydb;

import java.util.*;

public class DatabaseBackup {
    private final long timestamp;
    private final Map<String, Record> records;

    public DatabaseBackup(long timestamp) {
        this.timestamp = timestamp;
        this.records = new HashMap<>();
    }

    public void addRecord(String key, Record record) {
        records.put(key, record);
    }

    public void restore(Map<String, Record> database, long currentTimestamp) {
        database.clear();
        records.forEach((key, record) -> {
            Record adjustedRecord = new Record();
            record.getFieldsWithTTL().forEach((fieldName, fieldData) -> {
                String value = fieldData.getValue();
                long originalTimestamp = fieldData.getCreationTime();
                Long ttl = fieldData.getTTL();
                
                if (ttl != null) {
                    long remainingTTL = (originalTimestamp + ttl) - timestamp;
                    if (remainingTTL > 0) {
                        adjustedRecord.setFieldWithTTL(fieldName, value, currentTimestamp, remainingTTL);
                    }
                } else {
                    adjustedRecord.setFieldAt(fieldName, value, currentTimestamp);
                }
            });
            if (!adjustedRecord.isEmpty()) {
                database.put(key, adjustedRecord);
            }
        });
    }

    public int getRecordCount() {
        return records.size();
    }
}
