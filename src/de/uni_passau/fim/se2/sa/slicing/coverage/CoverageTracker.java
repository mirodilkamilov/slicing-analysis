package de.uni_passau.fim.se2.sa.slicing.coverage;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tracks line coverage information, i.e., which lines were executed and which were not.
 *
 * <p>The tracker is designed to track coverage information for a single class and a single
 * execution. It can be used for dynamic slicing.
 */
public final class CoverageTracker {

  private static final Set<Integer> visitedLines = new LinkedHashSet<>();

  private CoverageTracker() {}

  /**
   * Provides the set of visited lines (identified by their line numbers).
   *
   * @return The set of visited lines
   */
  public static Set<Integer> getVisitedLines() {
    return Collections.unmodifiableSet(visitedLines);
  }

  /**
   * Track a visit of a line.
   *
   * @param pLineNumber The line number that was executed
   */
  // Needs to be public to be callable during test execution
  public static void trackLineVisit(int pLineNumber) {
    synchronized (visitedLines) {
      visitedLines.add(pLineNumber);
    }
  }

  /**
   * Resets coverage information.
   *
   * <p>After calling this method, the set returned by {@link #getVisitedLines()} will be empty;
   * only intended for use during testing.
   */
  @VisibleForTesting
  public static void reset() {
    visitedLines.clear();
  }
}
