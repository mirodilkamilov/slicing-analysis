package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

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

    // Add nodes and edges from CDG
    for (Node source : cdg.getNodes()) {
      var successors = cdg.getSuccessors(source);
        pdg.addNode(source);
        for (Node target : successors) {
          pdg.addNode(target);
          pdg.addEdge(source, target);
        }
    }

    // Add nodes and edges from DDG
    for (Node source : ddg.getNodes()) {
      pdg.addNode(source);
      for (Node target : ddg.getSuccessors(source)) {
        pdg.addNode(target);
        pdg.addEdge(source, target);
      }
    }

    return pdg;
  }

  /** {@inheritDoc} */
  @Override
  public Set<Node> backwardSlice(Node pCriterion) {
    computeResult();
    Set<Node> slice = new HashSet<>();
    Deque<Node> worklist = new ArrayDeque<>();

    worklist.add(pCriterion);
    slice.add(pCriterion);

    while (!worklist.isEmpty()) {
      Node current = worklist.poll();
      for (Node pred : pdg.getPredecessors(current)) {
        if (slice.add(pred)) { // Only add if not already in slice
          worklist.add(pred);
        }
      }
    }

    return slice;
  }
}
