package de.uni_passau.fim.se2.sa.slicing.graph;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.output.NodeSorter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

/** Provides an analysis computing a post-dominator tree for a CFG. */
public class PostDominatorTree extends Graph {

  PostDominatorTree(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  PostDominatorTree(ProgramGraph pCFG) {
    super(pCFG);
  }

  /**
   * Computes the post-dominator tree of the method.
   *
   * <p>The implementation uses the {@link #cfg} graph as the starting point.
   *
   * @return The post-dominator tree of the control-flow graph
   */
  @Override
  public ProgramGraph computeResult() {
    ProgramGraph reversedCFG = reverseGraph(cfg);
    Node entry = reversedCFG.getEntry().orElseThrow(
            () -> new IllegalStateException("Entry node isn't present in the reversed CFG")
    );
    List<Node> allNodes = NodeSorter.sortReversedCFGInPostDominatorOrder(reversedCFG);

    Map<Node, Set<Node>> postDominators = initializePostDominators(allNodes, entry);

    // Iteratively updating each node's postdom set
    computeFixedPointPostDominators(allNodes, entry, postDominators);

    // Building Post-Dominator Tree
    return buildTreeFromPostDominators(allNodes, entry, postDominators);
  }

  private static Map<Node, Set<Node>> initializePostDominators(List<Node> allNodes, Node entry) {
    Map<Node, Set<Node>> postDominators = new HashMap<>();
    for (Node n : allNodes) {
      if (n.equals(entry)) {
        postDominators.put(n, Set.of(n));
      } else {
        postDominators.put(n, new HashSet<>(allNodes));
      }
    }
    return postDominators;
  }

  private static ProgramGraph buildTreeFromPostDominators(List<Node> allNodes, Node entry, Map<Node, Set<Node>> postDominators) {
    ProgramGraph pPDT = new ProgramGraph();
    pPDT.addNode(entry);
    Queue<Node> queue = new LinkedList<>();
    queue.add(entry);

    // Remove self nodes
    Map<Node, Set<Node>> strictPostDominators = new HashMap<>();
    for (Node n : allNodes) {
      Set<Node> postDomSet = new HashSet<>(postDominators.get(n));
      postDomSet.remove(n);
      strictPostDominators.put(n, postDomSet);
    }

    while (!queue.isEmpty()) {
      Node m = queue.poll(); // Get next node

      for (Node n : allNodes) {
        Set<Node> postDomCandidates = strictPostDominators.get(n);
        if (!postDomCandidates.isEmpty() && postDomCandidates.contains(m)) {
          postDomCandidates.remove(m);
          if (postDomCandidates.isEmpty()) {
            // Add n as child of m in PDT
            pPDT.addNode(n);
            pPDT.addEdge(m, n);
            queue.add(n);
          }
        }
      }
    }

    return pPDT;
  }

  private void computeFixedPointPostDominators(List<Node> allNodes, Node entry, Map<Node, Set<Node>> postDominators) {
    boolean changed;
    do {
      changed = false;
      for (Node n : allNodes) {
        if (n.equals(entry)) continue;

        // Get successors in original CFG
        Set<Node> successors = new HashSet<>(cfg.getSuccessors(n));

        List<Node> allNodesInCFGOrder = new ArrayList<>(cfg.getNodes());
        // Intersect post-dominator sets of all successors
        Set<Node> newPostDom = new LinkedHashSet<>(allNodesInCFGOrder);
        for (Node s : successors) {
          newPostDom.retainAll(postDominators.get(s));
        }
        newPostDom.add(n); // Every node post-dominates itself

        if (!newPostDom.equals(postDominators.get(n))) {
          postDominators.put(n, newPostDom);
          changed = true;
        }
      }
    } while (changed);
  }
}
