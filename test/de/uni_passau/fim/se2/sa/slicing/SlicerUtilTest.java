package de.uni_passau.fim.se2.sa.slicing;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SlicerUtilTest {

    @BeforeEach
    void clearCoverageBeforeTest() {
        CoverageTracker.reset();
    }

    @Test
    void testSimplify_ShouldRetainOnlyVisitedNodesAndEdges() {
        // Setup nodes with specific line numbers
        Node a = new Node("A"); // Line 1
        Node b = new Node("B"); // Line 2
        Node c = new Node("C"); // Line 3

        setLineNumber(a, 1);
        setLineNumber(b, 2);
        setLineNumber(c, 3);

        // Setup PDG with edges: A → B, B → C
        ProgramGraph pdgGraph = new ProgramGraph();
        pdgGraph.addNode(a);
        pdgGraph.addNode(b);
        pdgGraph.addNode(c);
        pdgGraph.addEdge(a, b);
        pdgGraph.addEdge(b, c);

        ProgramDependenceGraph pdg = new ProgramDependenceGraph(pdgGraph);

        // Mark only lines 1 and 2 as visited
        CoverageTracker.trackLineVisit(1);
        CoverageTracker.trackLineVisit(2);

        ProgramDependenceGraph simplified = SlicerUtil.simplify(pdg);
        ProgramGraph result = simplified.computeResult();

        // Should contain only nodes A and B, with edge A → B
        assertEquals(Set.of(a, b), result.getNodes(), "Simplified graph should contain only visited nodes");

        assertEquals(1, result.getEdges().size(), "Should contain exactly one edge");
        DefaultEdge edge = result.getEdges().iterator().next();
        assertEquals(a, result.getEdgeSource(edge));
        assertEquals(b, result.getEdgeTarget(edge));
    }

    @Test
    void testSimplify_WithNoVisitedLines_ShouldReturnEmptyGraph() {
        Node a = new Node("A");
        setLineNumber(a, 42);

        ProgramGraph pdgGraph = new ProgramGraph();
        pdgGraph.addNode(a);

        ProgramDependenceGraph pdg = new ProgramDependenceGraph(pdgGraph);

        ProgramDependenceGraph simplified = SlicerUtil.simplify(pdg);
        assertTrue(simplified.computeResult().getNodes().isEmpty(), "Simplified graph should be empty");
    }

    // Helper to set line numbers using reflection if no setter exists
    private void setLineNumber(Node node, int lineNumber) {
        try {
            var field = Node.class.getDeclaredField("lineNumber");
            field.setAccessible(true);
            field.setInt(node, lineNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testExecuteTest_WithInvalidClass_ShouldThrowRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                SlicerUtil.executeTest("NonExistentClass", "nonExistentMethod")
        );

        assertTrue(ex.getMessage().contains("Test class or method not found"));
    }

    @Test
    void testExecuteTest_WithInvalidMethod_ShouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () ->
                SlicerUtil.executeTest(this.getClass().getName(), "nonExistentMethod")
        );
    }

}
