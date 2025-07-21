package de.uni_passau.fim.se2.sa.slicing.graph;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.slicing.cfg.CFGLocalVariableTableVisitor;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class DataDependenceGraphTest {

    @Test
    void testComputeResult_IsOddCFG_ShouldReturnDDG() throws IOException {
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

        DataDependenceGraph ddg = new DataDependenceGraph(classNode, methodNode);
        ProgramGraph result = ddg.computeResult();
        assertEquals(5, result.getNodes().size(), "Unexpected number of nodes in DDG");
        assertEquals(3, result.getEdges().size(), "Unexpected number of edges in DDG");

        Set<String> expectedNodeIds = Set.of(
                "\"LABEL1  line number: -1\"",
                "\"IFEQ14  line number: 7\"",
                "\"ISTORE23  line number: 8\"",
                "\"IRETURN20  line number: 10\"",
                "\"ISTORE8  line number: 6\""
        );
        Set<String> actualNodeIds = result.getNodes().stream()
                .map(Node::toString)
                .collect(Collectors.toSet());
        assertEquals(expectedNodeIds, actualNodeIds, "Mismatch in expected nodes");

        assertTimeoutPreemptively(Duration.ofMillis(100), ddg::computeResult);
    }

}
