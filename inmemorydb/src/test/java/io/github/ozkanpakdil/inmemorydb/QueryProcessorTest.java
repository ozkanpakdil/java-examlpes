package io.github.ozkanpakdil.inmemorydb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class QueryProcessorTest {
    private QueryProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new QueryProcessor();
    }

    @Test
    void testLevel1Operations() {
        List<List<String>> queries = Arrays.asList(
            Arrays.asList("SET", "A", "B", "E"),
            Arrays.asList("SET", "A", "C", "F"),
            Arrays.asList("GET", "A", "B"),
            Arrays.asList("GET", "A", "D"),
            Arrays.asList("DELETE", "A", "B"),
            Arrays.asList("DELETE", "A", "D")
        );

        List<String> expectedResults = Arrays.asList("", "", "E", "", "true", "false");
        List<String> actualResults = processor.processQueries(queries);
        
        assertEquals(expectedResults, actualResults);
    }

    @Test
    void testLevel2Operations() {
        List<List<String>> queries = Arrays.asList(
            Arrays.asList("SET", "A", "BC", "E"),
            Arrays.asList("SET", "A", "BD", "F"),
            Arrays.asList("SET", "A", "C", "G"),
            Arrays.asList("SCAN_BY_PREFIX", "A", "B"),
            Arrays.asList("SCAN", "A"),
            Arrays.asList("SCAN_BY_PREFIX", "B", "B")
        );

        List<String> expectedResults = Arrays.asList(
            "", "", "", "BC(E), BD(F)", "BC(E), BD(F), C(G)", ""
        );
        List<String> actualResults = processor.processQueries(queries);
        
        assertEquals(expectedResults, actualResults);
    }

    @Test
    void testLevel3Operations() {
        List<List<String>> queries = Arrays.asList(
            Arrays.asList("SET_AT_WITH_TTL", "A", "BC", "E", "1", "9"),
            Arrays.asList("SET_AT_WITH_TTL", "A", "BC", "E", "5", "10"),
            Arrays.asList("SET_AT", "A", "BD", "F", "5"),
            Arrays.asList("SCAN_BY_PREFIX_AT", "A", "B", "14"),
            Arrays.asList("SCAN_BY_PREFIX_AT", "A", "B", "15")
        );

        List<String> expectedResults = Arrays.asList(
            "", "", "", "BC(E), BD(F)", "BD(F)"
        );
        List<String> actualResults = processor.processQueries(queries);
        
        assertEquals(expectedResults, actualResults);
    }

    @Test
    void testLevel4Operations() {
        List<List<String>> queries = Arrays.asList(
            Arrays.asList("SET_AT_WITH_TTL", "A", "B", "C", "1", "10"),
            Arrays.asList("BACKUP", "3"),
            Arrays.asList("SET_AT", "A", "D", "E", "4"),
            Arrays.asList("BACKUP", "5"),
            Arrays.asList("DELETE_AT", "A", "B", "8"),
            Arrays.asList("BACKUP", "9"),
            Arrays.asList("RESTORE", "10", "7"),
            Arrays.asList("BACKUP", "11"),
            Arrays.asList("SCAN_AT", "A", "15"),
            Arrays.asList("SCAN_AT", "A", "16")
        );

        List<String> expectedResults = Arrays.asList(
            "", "1", "", "1", "true", "1", "", "1", "B(C), D(E)", "D(E)"
        );
        List<String> actualResults = processor.processQueries(queries);
        
        assertEquals(expectedResults, actualResults);
    }
}
