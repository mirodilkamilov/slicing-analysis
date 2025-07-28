package de.uni_passau.fim.se2.sa.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsOddTest {
    private IsOdd isOdd;

    @BeforeEach
    void setUp() {
        isOdd = new IsOdd();
    }

    @Test
    void isOdd_OddNumber_ShouldReturnTrue() {
        assertTrue(isOdd.isOdd(3));
    }

    @Test
    void isOdd_EvenNumber_ShouldReturnFalse() {
        assertFalse(isOdd.isOdd(4));
    }
}
