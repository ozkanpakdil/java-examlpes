package io.github.ozkanpakdil.inmemorydb;

import java.util.List;
import java.util.ArrayList;

public class QueryProcessor {
    private final InMemoryDB db;

    public QueryProcessor() {
        this.db = new InMemoryDB();
    }

    public List<String> processQueries(List<List<String>> queries) {
        List<String> results = new ArrayList<>();
        
        for (List<String> query : queries) {
            String command = query.get(0);
            String result;
            
            try {
                result = switch (command) {
                    case "SET" -> db.set(query.get(1), query.get(2), query.get(3));
                    case "GET" -> db.get(query.get(1), query.get(2));
                    case "DELETE" -> db.delete(query.get(1), query.get(2));
                    case "SCAN" -> db.scan(query.get(1));
                    case "SCAN_BY_PREFIX" -> db.scanByPrefix(query.get(1), query.get(2));
                    case "SET_AT" -> db.setAt(query.get(1), query.get(2), query.get(3),
                            Long.parseLong(query.get(4)));
                    case "SET_AT_WITH_TTL" -> db.setAtWithTTL(query.get(1), query.get(2), query.get(3),
                            Long.parseLong(query.get(4)), Long.parseLong(query.get(5)));
                    case "GET_AT" -> db.getAt(query.get(1), query.get(2),
                            Long.parseLong(query.get(3)));
                    case "DELETE_AT" -> db.deleteAt(query.get(1), query.get(2),
                            Long.parseLong(query.get(3)));
                    case "SCAN_AT" -> db.scanAt(query.get(1), Long.parseLong(query.get(2)));
                    case "SCAN_BY_PREFIX_AT" -> db.scanByPrefixAt(query.get(1), query.get(2),
                            Long.parseLong(query.get(3)));
                    case "BACKUP" -> db.backup(Long.parseLong(query.get(1)));
                    case "RESTORE" -> db.restore(Long.parseLong(query.get(1)),
                            Long.parseLong(query.get(2)));
                    default -> "Unknown command: " + command;
                };
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
            
            results.add(result);
        }
        
        return results;
    }
}
