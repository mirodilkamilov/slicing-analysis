package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GCDTest {

  private GCD gcd;

  @BeforeEach
  void setUp() {
    gcd = new GCD();
  }

  @Test
  void testGcd1() {
    final int expected = 42;
    final int actual = gcd.gcd(42, 0);
    assertEquals(expected, actual);
  }

  @Test
  void testGcd2() {
    final int expected = 7;
    final int actual = gcd.gcd(42, 7);
    assertEquals(expected, actual);
  }
}
