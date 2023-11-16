package org.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class Q1Test {
    Q1 testee = new Q1();

    @Test
    void enqueue() {
        for (int i = 0; i < 10; i++) {
            testee.enqueue(i);
        }
        System.out.println(testee.deque());
        for (int i = 11; i < 22; i++) {
            testee.enqueue(i);
        }
        System.out.println(testee.deque());
    }

    @Test
    void deque() {

    }
}