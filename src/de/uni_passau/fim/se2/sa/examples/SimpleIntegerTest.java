package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleIntegerTest {

  private SimpleInteger i;

  @BeforeEach
  void setUp() {
    i = new SimpleInteger();
  }

  @Test
  void testFoo() {
    final int expected = -14;
    final int actual = i.foo();
    assertEquals(expected, actual);
  }
}
