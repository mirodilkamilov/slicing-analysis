package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** Provides an analysis that calculates the program-dependence graph. */
public class ProgramDependenceGraph extends Graph implements Sliceable<Node> {

  private ProgramGraph pdg;
  private final ProgramGraph cdg;
  private final ProgramGraph ddg;

  public ProgramDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
    pdg = null;

    if (cfg != null) {
      cdg = new ControlDependenceGraph(pClassNode, pMethodNode).computeResult();
      ddg = new DataDependenceGraph(pClassNode, pMethodNode).computeResult();
    } else {
      cdg = null;
      ddg = null;
    }
  }

  public ProgramDependenceGraph(ProgramGraph pProgramGraph) {
    super(null);
    pdg = pProgramGraph;
    cdg = null;
    ddg = null;
  }

  /**
   * Computes the program-dependence graph from a control-flow graph.
   *
   * <p>You may wish to use the {@link ControlDependenceGraph} and {@link DataDependenceGraph} you
   * have already implemented to support computing the program-dependence graph.
   *
   * @return A program-dependence graph.
   */
  @Override
  public ProgramGraph computeResult() {
    if (pdg != null) {
      return pdg;
    }
    if (cdg == null || ddg == null) {
      throw new IllegalStateException("CDG and DDG must be computed before building PDG");
    }

    pdg = new ProgramGraph();

    // Step 1: Add all nodes from CDG
    for (Node node : cdg.getNodes()) {
      pdg.addNode(node);
    }

    // Step 2: Add all nodes from DDG
    for (Node node : ddg.getNodes()) {
      pdg.addNode(node);
    }

    // Step 3: Add all edges from CDG
    for (Node source : cdg.getNodes()) {
      for (Node target : cdg.getSuccessors(source)) {
        pdg.addEdge(source, target);
      }
    }

    // Step 4: Add all edges from DDG
    for (Node source : ddg.getNodes()) {
      for (Node target : ddg.getSuccessors(source)) {
        pdg.addEdge(source, target);
      }
    }

    return pdg;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Node> backwardSlice(Node pCriterion) {
    // TODO Implement me
//    throw new UnsupportedOperationException("Implement me");
    computeResult();
    return Set.of();
  }
}
