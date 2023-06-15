package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RationalTest {

  @Test
  void testOfString() {
    final var expected = Rational.of(1);
    final var actual = Rational.ofString("1");
    assertEquals(expected, actual);
  }
}
