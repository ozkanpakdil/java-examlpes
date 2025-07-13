package io.github.ozkanpakdil.inmemorydb;

public record Field(String value, long creationTime, Long ttl) {
    public Field(String value) {
        this(value, System.currentTimeMillis(), null);
    }

    public Field(String value, long timestamp) {
        this(value, timestamp, null);
    }

    public Field(String value, long timestamp, long ttl) {
        this(value, timestamp, ttl > 0 ? ttl : null);
    }

    public String getValue() {
        return value;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Long getTTL() {
        return ttl;
    }

    public boolean isExpired(long currentTime) {
        return ttl == null || (creationTime + ttl > currentTime);
    }

    public Field copy() {
        if (ttl != null) {
            return new Field(value, creationTime, ttl);
        } else if (creationTime > 0) {
            return new Field(value, creationTime);
        }
        return new Field(value);
    }
}
