package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GraphTest {

    @Mock
    private ProgramGraph mockProgramGraph;

    @Test
    void testReverseGraph_ShouldNotRunTooLong() {
        ProgramGraph cfg = new ProgramGraph();
        Node a = new Node("A");
        Node b = new Node("B");
        cfg.addNode(a);
        cfg.addNode(b);
        cfg.addEdge(a, b);
        cfg.addEdge(b, a);

        Graph dummyGraph = new DummyGraph(cfg);

        assertTimeoutPreemptively(Duration.ofMillis(100), dummyGraph::computeResult);
    }

    @Test
    void testReverseGraph_ShouldReverseEdges() {
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        ProgramGraph original = new ProgramGraph();
        original.addNode(a);
        original.addNode(b);
        original.addNode(c);
        original.addEdge(a, b);
        original.addEdge(b, c);

        Graph graph = new DummyGraph(original); // Dummy subclass for testing

        ProgramGraph reversed = graph.reverseGraph(original);

        assertTrue(reversed.getSuccessors(b).contains(a));
        assertTrue(reversed.getSuccessors(c).contains(b));
        assertTrue(reversed.getSuccessors(a).isEmpty());
    }

    @Test
    void testConstructorWithClassNode_WhenClassNodeIsNull_ShouldSetFieldsToNull() {
        Graph graph = new DummyGraph(null, null);
        assertNull(graph.classNode);
        assertNull(graph.methodNode);
        assertNull(graph.cfg);
    }

    @Test
    void testConstructorWithClassNode_WhenClassNodeIsNotNull_ShouldExtractCFG() {
        ProgramGraph programGraph = new ProgramGraph();

        Graph graph = new DummyGraph(programGraph);

        assertEquals(programGraph, graph.getCFG());
        assertNull(graph.classNode);
        assertNull(graph.methodNode);
    }

    @Test
    void testConstructorWithCFG() {
        Graph graph = new DummyGraph(mockProgramGraph);
        assertEquals(mockProgramGraph, graph.getCFG());
    }

    // Dummy subclass to allow instantiation
    static class DummyGraph extends Graph {
        DummyGraph(ClassNode pClassNode, MethodNode pMethodNode) {
            super(pClassNode, pMethodNode);
        }

        DummyGraph(ProgramGraph pCFG) {
            super(pCFG);
        }

        @Override
        public ProgramGraph computeResult() {
            if (cfg != null) {
                return reverseGraph(cfg);
            } else {
                return new ProgramGraph();
            }
        }
    }
}
