package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.DefUseAnalyzer;
import br.usp.each.saeg.asm.defuse.DefUseFrame;
import br.usp.each.saeg.asm.defuse.Variable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/** Provides a simple data-flow analysis. */
class DataFlowAnalysis {

  private DataFlowAnalysis() {}

  /**
   * Provides the collection of {@link Variable}s that are used by the given instruction.
   *
   * @param pOwningClass The class that owns the method
   * @param pMethodNode The method that contains the instruction
   * @param pInstruction The instruction
   * @return The collection of {@link Variable}s that are used by the given instruction
   * @throws AnalyzerException In case an error occurs during the analysis
   */
  static Collection<Variable> usedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    DefUseFrame frame = getCorrespondingDefUseFrame(pOwningClass, pMethodNode, pInstruction);
    if (frame == null) {
      return Collections.emptyList();
    }
    return new HashSet<>(frame.getUses());
  }

  /**
   * Provides the collection of {@link Variable}s that are defined by the given instruction.
   *
   * @param pOwningClass The class that owns the method
   * @param pMethodNode The method that contains the instruction
   * @param pInstruction The instruction
   * @return The collection of {@link Variable}s that are defined by the given instruction
   * @throws AnalyzerException In case an error occurs during the analysis
   */
  static Collection<Variable> definedBy(
      String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction)
      throws AnalyzerException {
    DefUseFrame frame = getCorrespondingDefUseFrame(pOwningClass, pMethodNode, pInstruction);
    if (frame == null) {
      return Collections.emptyList();
    }
    return new HashSet<>(frame.getDefinitions());
  }

  private static DefUseFrame getCorrespondingDefUseFrame(String pOwningClass, MethodNode pMethodNode, AbstractInsnNode pInstruction) throws AnalyzerException {
    if (pInstruction == null) {
      return null;
    }

    DefUseAnalyzer analyzer = new DefUseAnalyzer();
    DefUseFrame[] frames = analyzer.analyze(pOwningClass, pMethodNode);
    int index = pMethodNode.instructions.indexOf(pInstruction);
    if (index < 0 || frames[index] == null) {
      return null;
    }

    return frames[index];
  }
}
