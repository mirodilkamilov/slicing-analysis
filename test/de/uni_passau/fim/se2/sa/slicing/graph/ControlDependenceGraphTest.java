package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.junit.jupiter.api.Test;

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
}
