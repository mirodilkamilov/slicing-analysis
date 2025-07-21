package de.uni_passau.fim.se2.sa.slicing.graph;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.slicing.cfg.CFGLocalVariableTableVisitor;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ProgramDependenceGraphTest {

    @Test
    void testConstructorWithPrecomputedPDG_ShouldReturnPrecomputedResult() {
        ProgramGraph mockPDG = new ProgramGraph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        mockPDG.addNode(nodeA);
        mockPDG.addNode(nodeB);
        mockPDG.addEdge(nodeA, nodeB);

        ProgramDependenceGraph pdg = new ProgramDependenceGraph(mockPDG);
        ProgramGraph result = pdg.computeResult();
        assertEquals(mockPDG, result, "PDG should return the precomputed instance");
    }

    @Test
    void testComputeResult_IsOddCFG_ShouldReturnPDG() throws IOException {
        final int apiLevel = Opcodes.ASM9;
        final ClassNode classNode = new ClassNode(apiLevel);
        final ClassReader classReader = new ClassReader("de.uni_passau.fim.se2.sa.examples.IsOdd");
        classReader.accept(classNode, 0);
        String methodName = "isOdd";
        String methodDescriptor = "(I)Z";

        final CFGLocalVariableTableVisitor visitor = new CFGLocalVariableTableVisitor(apiLevel);
        classReader.accept(visitor, 0);

        MethodNode methodNode = classNode.methods.stream()
                .filter(m -> methodName.equals(m.name) && methodDescriptor.equals(m.desc))
                .findAny()
                .orElse(null);
        Preconditions.checkNotNull(methodNode, "Could not find an appropriate method!");

        ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
        ProgramGraph result = pdg.computeResult();
        assertEquals(25, result.getNodes().size(), "Unexpected number of nodes in PDG");
        assertEquals(7, result.getEdges().size(), "Unexpected number of edges in PDG");

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
                Pair.of("\"IFEQ14  line number: 7\"", "\"ISTORE23  line number: 8\""),
                Pair.of("\"LABEL1  line number: -1\"", "\"IFEQ14  line number: 7\""),
                Pair.of("\"ISTORE23  line number: 8\"", "\"IRETURN20  line number: 10\""),
                Pair.of("\"ISTORE8  line number: 6\"", "\"IRETURN20  line number: 10\"")
        );
        assertEquals(expectedEdges, actualEdges, "Mismatch in expected control dependence edges");

        assertTimeoutPreemptively(Duration.ofMillis(100), pdg::computeResult);
    }

    @Test
    void testBackwardSlice_ShouldReturnTransitivePredecessorsIncludingCriterion() {
        ProgramGraph pdgGraph = new ProgramGraph();

        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");

        pdgGraph.addNode(a);
        pdgGraph.addNode(b);
        pdgGraph.addNode(c);

        pdgGraph.addEdge(a, b);  // A → B
        pdgGraph.addEdge(b, c);  // B → C

        ProgramDependenceGraph pdg = new ProgramDependenceGraph(pdgGraph);
        Set<Node> slice = pdg.backwardSlice(c);
        assertEquals(Set.of(a, b, c), slice, "Backward slice should include transitive predecessors and criterion");
    }

    @Test
    void testBackwardSlice_OnIsolatedNode_ShouldReturnSingletonSet() {
        ProgramGraph pdgGraph = new ProgramGraph();

        Node isolated = new Node("Isolated");
        pdgGraph.addNode(isolated);

        ProgramDependenceGraph pdg = new ProgramDependenceGraph(pdgGraph);
        Set<Node> slice = pdg.backwardSlice(isolated);
        assertEquals(Set.of(isolated), slice, "Backward slice of isolated node should only include itself");
    }

}

















