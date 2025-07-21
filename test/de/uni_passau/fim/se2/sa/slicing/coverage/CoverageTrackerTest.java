package de.uni_passau.fim.se2.sa.slicing.coverage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageTrackerTest {

    @BeforeEach
    void clearCoverageBeforeTest() {
        CoverageTracker.reset();
    }

    @Test
    void testTrackLineVisit_ShouldRecordLine() {
        CoverageTracker.trackLineVisit(42);
        Set<Integer> visited = CoverageTracker.getVisitedLines();
        assertEquals(Set.of(42), visited, "Visited lines should contain 42 after tracking it");
    }

    @Test
    void testTrackLineVisit_ShouldRecordMultipleLines() {
        CoverageTracker.trackLineVisit(10);
        CoverageTracker.trackLineVisit(20);
        CoverageTracker.trackLineVisit(30);

        Set<Integer> visited = CoverageTracker.getVisitedLines();
        assertEquals(Set.of(10, 20, 30), visited, "Visited lines should contain all tracked lines");
    }

    @Test
    void testTrackLineVisit_ShouldNotAddDuplicates() {
        CoverageTracker.trackLineVisit(99);
        CoverageTracker.trackLineVisit(99);

        Set<Integer> visited = CoverageTracker.getVisitedLines();
        assertEquals(Set.of(99), visited, "Visited lines should not contain duplicates");
    }

    @Test
    void testReset_ShouldClearVisitedLines() {
        CoverageTracker.trackLineVisit(1);
        CoverageTracker.trackLineVisit(2);

        CoverageTracker.reset();
        assertTrue(CoverageTracker.getVisitedLines().isEmpty(), "Visited lines should be empty after reset");
    }
}
