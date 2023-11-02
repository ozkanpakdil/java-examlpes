package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LRUCacheTest {

    LRUCache testee;

    @BeforeEach
    void setup() {
        testee = new LRUCache(1);
    }

    @Test
    void testSuccessInsertGet() {
        testee.put(1, 1);
        assertEquals(1, testee.get(1));
    }

    @Test
    void testSuccessCapacity() {
        for (int i = 0; i < 20; i++) {
            testee.put(i, 1);
        }
        // over capacity put result should be default value
        assertEquals(-1, testee.get(1));
    }

}