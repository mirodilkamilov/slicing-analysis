package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ControlDependenceGraph extends Graph {
  private PostDominatorTree pdt = new PostDominatorTree(new ProgramGraph());

  ControlDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
    pdt = new PostDominatorTree(pClassNode, pMethodNode);
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
    ProgramGraph pPDT = pdt.computeResult();
    ProgramGraph pCDG = new ProgramGraph();
    Set<DefaultEdge> allEdges = cfg.getEdges();
    Set<DefaultEdge> cfgEdgesNotInPdt = new HashSet<>();

    for (DefaultEdge edge : allEdges) {
      if (!cfg.isEdgeReversedInOtherGraph(edge, pPDT)) {
        cfgEdgesNotInPdt.add(edge);
      }
    }

    for (DefaultEdge edge : cfgEdgesNotInPdt) {
      Node source = cfg.getEdgeSource(edge);
      Node target = cfg.getEdgeTarget(edge);
      Node leastCommonAncestor = pPDT.getLeastCommonAncestor(source, target);

      // TODO: there might be two predecessors and two leastCommonAncestors (while visiting upwards)
      pCDG.addNode(target);
      Collection<Node> visitedNodes = pPDT.getTransitivePredecessorsUntilAncestor(target, leastCommonAncestor);
      pCDG.addNode(source);
      pCDG.addEdge(source, target);
      for (Node n : visitedNodes) {
        pCDG.addNode(n);
        pCDG.addEdge(source, n);
      }
    }

    return pCDG;
  }
}
