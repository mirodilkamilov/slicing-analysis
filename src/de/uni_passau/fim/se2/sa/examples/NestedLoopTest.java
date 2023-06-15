package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NestedLoopTest {

  private NestedLoop l;

  @BeforeEach
  void setUp() {
    l = new NestedLoop();
  }

  @Test
  void testNestedWhileForLoops() {
    final int expected = 56;
    final int actual = l.nestedWhileForLoops();
    assertEquals(expected, actual);
  }

  @Test
  void testPrivate() {
    final int expected = 56;
    final int actual = l.nestedForLoops();
    assertEquals(expected, actual);
  }
}
