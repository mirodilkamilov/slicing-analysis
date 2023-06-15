package de.uni_passau.fim.se2.sa.examples;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculatorTest {
  private Calculator calculator;

  @BeforeEach
  void setUp() {
    calculator = new Calculator();
  }

  @Test
  void testEvaluateEmpty() {
    final int expected = 0;
    final int actual = calculator.evaluate("+");
    assertEquals(expected, actual);
  }

  @Test
  void testEvaluateMultipleSummands() {
    final int expected = 42;
    final int actual = calculator.evaluate("3+4+5+6+7+8+9");
    assertEquals(expected, actual);
  }

  @Test
  void testPrivate() {
    final int expected = 23;
    final int actual = calculator.evaluate("12+11");
    assertEquals(expected, actual);
  }
}
