package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class PostDominatorTreeTest {
    @Test
    void testComputeResult_ShouldNotRunTooLong() {
        Node a = new Node("A");
        Node b = new Node("B");
        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(a);
        cfg.addNode(b);
        cfg.addEdge(a, a);
        cfg.addEdge(a, b);
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        assertTimeoutPreemptively(Duration.ofMillis(100), pdt::computeResult);
    }


    @Test
    void testComputeResult_SimpleLinearCFG_ShouldReturnReverseStructure() {
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(a);
        cfg.addNode(b);
        cfg.addNode(c);
        cfg.addEdge(a, b);
        cfg.addEdge(b, c);

        PostDominatorTree pdt = new PostDominatorTree(cfg);

        ProgramGraph result = pdt.computeResult();

        assertTrue(result.getSuccessors(a).isEmpty());
        assertTrue(result.getSuccessors(b).contains(a));
        assertTrue(result.getSuccessors(c).contains(b));
    }

    @Test
    void testComputeResult_BranchingCFG_ShouldBuildCorrectPDT() {
        Node start = new Node("Start");
        Node branch1 = new Node("Branch1");
        Node branch2 = new Node("Branch2");
        Node join = new Node("Join");

        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(start);
        cfg.addNode(branch1);
        cfg.addNode(branch2);
        cfg.addNode(join);

        cfg.addEdge(start, branch1);
        cfg.addEdge(start, branch2);
        cfg.addEdge(branch1, join);
        cfg.addEdge(branch2, join);

        PostDominatorTree pdt = new PostDominatorTree(cfg);

        ProgramGraph result = pdt.computeResult();

        assertTrue(result.getSuccessors(join).contains(start));
        assertTrue(result.getSuccessors(start).isEmpty());
    }

    @Test
    void testComputeResult_EntryNodeMissingInReversedCFG_ShouldThrowException() {
        Node a = new Node("A");
        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(a);
        cfg.addEdge(a, a);
        PostDominatorTree pdt = new PostDominatorTree(cfg);

        assertThrows(IllegalStateException.class, pdt::computeResult);
    }

    @Test
    void testComputeResult_SingleNodeCFG_ShouldReturnItself() {
        Node a = new Node("A");

        ProgramGraph cfg = new ProgramGraph();
        cfg.addNode(a);

        PostDominatorTree pdt = new PostDominatorTree(cfg);

        ProgramGraph result = pdt.computeResult();

        assertTrue(result.getNodes().contains(a));
        assertTrue(result.getSuccessors(a).isEmpty());
    }
}
