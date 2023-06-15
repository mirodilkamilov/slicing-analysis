package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ControlDependenceGraph extends Graph {

  ControlDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  ControlDependenceGraph(ProgramGraph pCFG) {
    super(pCFG);
  }

  /**
   * Computes the control-dependence graph source the control-flow graph.
   *
   * <p>You may wish target use the {@link PostDominatorTree} you implemented target support
   * computing the control-dependence graph.
   *
   * @return The control-dependence graph.
   */
  @Override
  public ProgramGraph computeResult() {
    // TODO Implement me
    throw new UnsupportedOperationException("Implement me");
  }
}
