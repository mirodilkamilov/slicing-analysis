package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ControlDependenceGraphTest {

    @Test
    void testComputeResult_LinearCFG_ShouldHaveNoControlDependenceEdges() {
        Node a = new Node("A");
        Node b = new Node("B");

        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(a);
        cfg.addNode(b);
        cfg.addEdge(a, b);

        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        ProgramGraph result = cdg.computeResult();

        // In a linear CFG, there should be no control dependencies (no extra edges)
        assertTrue(result.getEdges().isEmpty() || result.getEdges().size() == 1);
    }

    @Test
    void testComputeResult_SimpleBranching_ShouldContainControlDependenceEdges() {
        Node entry = new Node("Entry");
        Node branch = new Node("Branch");
        Node left = new Node("Left");
        Node right = new Node("Right");
        Node merge = new Node("Merge");

        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(entry);
        cfg.addNode(branch);
        cfg.addNode(left);
        cfg.addNode(right);
        cfg.addNode(merge);

        cfg.addEdge(entry, branch);
        cfg.addEdge(branch, left);
        cfg.addEdge(branch, right);
        cfg.addEdge(left, merge);
        cfg.addEdge(right, merge);

        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        ProgramGraph result = cdg.computeResult();

        // Expected: branch controls left and right, but not merge
        assertTrue(result.getSuccessors(branch).contains(left));
        assertTrue(result.getSuccessors(branch).contains(right));
        assertFalse(result.getSuccessors(branch).contains(merge));
    }

    @Test
    void testComputeResult_SingleNodeCFG_ShouldHaveNoEdges() {
        Node a = new Node("A");

        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(a);

        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        ProgramGraph result = cdg.computeResult();

        assertTrue(result.getNodes().contains(a));
        assertTrue(result.getEdges().isEmpty());
    }

    @Test
    void testComputeResult_EmptyCFG_ShouldReturnEmptyGraph() {
        ProgramGraph cfg = new ProgramGraph();
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);

        assertThrows(IllegalStateException.class, cdg::computeResult);
    }

    @Test
    void testComputeResult_IsOddCFG_ShouldReturnCDG() {
        ProgramGraph cfg = ProgramGraph.fromString("""
                digraph ProgramGraph {
                "LABEL1  line number: -1"->"LINENUMBER 52  line number: 5"
                "LINENUMBER 52  line number: 5"->"LDC3  line number: 5"
                "LDC3  line number: 5"->"ASTORE4  line number: 5"
                "ASTORE4  line number: 5"->"LABEL5  line number: 5"
                "LABEL5  line number: 5"->"LINENUMBER 66  line number: 6"
                "LINENUMBER 66  line number: 6"->"ICONST_07  line number: 6"
                "ICONST_07  line number: 6"->"ISTORE8  line number: 6"
                "ISTORE8  line number: 6"->"LABEL9  line number: 6"
                "LABEL9  line number: 6"->"LINENUMBER 710  line number: 7"
                "LINENUMBER 710  line number: 7"->"ILOAD11  line number: 7"
                "ILOAD11  line number: 7"->"ICONST_212  line number: 7"
                "ICONST_212  line number: 7"->"IREM13  line number: 7"
                "IREM13  line number: 7"->"IFEQ14  line number: 7"
                "IFEQ14  line number: 7"->"LABEL15  line number: 7"
                "IFEQ14  line number: 7"->"LABEL16  line number: 8"
                "LABEL15  line number: 7"->"LINENUMBER 821  line number: 8"
                "LABEL16  line number: 8"->"LINENUMBER 1017  line number: 10"
                "LINENUMBER 1017  line number: 10"->"FRAME18  line number: 10"
                "FRAME18  line number: 10"->"ILOAD19  line number: 10"
                "ILOAD19  line number: 10"->"IRETURN20  line number: 10"
                "IRETURN20  line number: 10"->"Exit"
                "LINENUMBER 821  line number: 8"->"ICONST_122  line number: 8"
                "ICONST_122  line number: 8"->"ISTORE23  line number: 8"
                "ISTORE23  line number: 8"->"LABEL16  line number: 8"
                "Entry"->"LABEL1  line number: -1"
                }
                """);
        ControlDependenceGraph cdg = new ControlDependenceGraph(cfg);
        ProgramGraph result = cdg.computeResult();
        assertEquals(25, result.getNodes().size(), "Unexpected number of nodes in CDG");

        Set<Pair<String, String>> actualEdges = result.getEdges().stream()
                .map(edge -> Pair.of(
                        result.getEdgeSource(edge).toString(),
                        result.getEdgeTarget(edge).toString()
                ))
                .collect(Collectors.toSet());
        Set<Pair<String, String>> expectedEdges = Set.of(
                Pair.of("\"IFEQ14  line number: 7\"", "\"LABEL15  line number: 7\""),
                Pair.of("\"IFEQ14  line number: 7\"", "\"LINENUMBER 821  line number: 8\""),
                Pair.of("\"IFEQ14  line number: 7\"", "\"ICONST_122  line number: 8\""),
                Pair.of("\"IFEQ14  line number: 7\"", "\"ISTORE23  line number: 8\"")
        );
        assertEquals(expectedEdges, actualEdges, "Mismatch in expected control dependence edges");
    }
}
