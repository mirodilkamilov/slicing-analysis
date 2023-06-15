package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestClassTest {
  private TestClass t;

  @BeforeEach
  void setUp() {
    t = new TestClass();
  }

  @Test
  void testCountFoos0() {
    final int expected = 0;
    final int actual = t.countFoos(0);
    assertEquals(expected, actual);
  }
}
