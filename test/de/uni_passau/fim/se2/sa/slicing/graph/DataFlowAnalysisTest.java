package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseFrame;
import br.usp.each.saeg.asm.defuse.Local;
import br.usp.each.saeg.asm.defuse.Variable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DataFlowAnalysisTest {

    @Mock
    MethodNode mockMethodNode;

    @Test
    void testDefinedBy_WhenInstructionIsNull_ShouldReturnEmpty() throws Exception {
        Collection<Variable> used = DataFlowAnalysis.definedBy("TestClass", mockMethodNode, null);
        assertTrue(used.isEmpty());
    }

    @Test
    void testDefinedBy_WithValidMethodSetup_ShouldNotThrow() throws Exception {
        InsnList insnList = new InsnList();
        AbstractInsnNode nopInsn = new InsnNode(Opcodes.NOP);
        insnList.add(nopInsn);
        insnList.add(new InsnNode(Opcodes.RETURN)); // Ensure valid exit

        MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "testMethod", "()V", null, null);
        methodNode.instructions = insnList;
        methodNode.tryCatchBlocks = new ArrayList<>();
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;

        Collection<Variable> defs = DataFlowAnalysis.definedBy("TestClass", methodNode, nopInsn);

        assertNotNull(defs);
    }


    @Test
    void testUsedBy_WhenInstructionIsNull_ShouldReturnEmpty() throws Exception {
        Collection<Variable> used = DataFlowAnalysis.usedBy("TestClass", mockMethodNode, null);
        assertTrue(used.isEmpty());
    }

    @Test
    void testUsedBy_WithValidMethodSetup_ShouldNotThrow() throws Exception {
        InsnList insnList = new InsnList();
        AbstractInsnNode nopInsn = new InsnNode(Opcodes.NOP);
        insnList.add(nopInsn);
        insnList.add(new InsnNode(Opcodes.RETURN)); // Ensure valid exit instruction

        MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "testMethod", "()V", null, null);
        methodNode.instructions = insnList;
        methodNode.tryCatchBlocks = new ArrayList<>();
        methodNode.maxLocals = 1;
        methodNode.maxStack = 1;

        Collection<Variable> used = DataFlowAnalysis.usedBy("TestClass", methodNode, nopInsn);

        assertNotNull(used);
    }

}
