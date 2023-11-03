package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Q2Test {

    Q2 testee = new Q2();

    @Test
    void reverse() {
        assertEquals("cba", testee.reverse("abc"));
        assertEquals("abc-abc", testee.reverse("cba-cba"));
        assertEquals("abcd-efg", testee.reverse("gfed-cba"));
    }
}