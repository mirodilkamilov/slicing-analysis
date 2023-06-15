package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ComplexTest {

  @Test
  void testToString() {
    final var c = new Complex(23, 42);
    final var expected = "23.0 + 42.0 i";
    final var actual = c.toString(0);
    assertEquals(expected, actual);
  }

  @Test
  void testPrivate2() {
    final Complex c = new Complex(23, 42);
    final Complex d = new Complex(42, 23);
    final Complex expected = new Complex(0, 2293);
    assertEquals(expected.toString(0), c.multiply(d).toString(0));
  }
}
