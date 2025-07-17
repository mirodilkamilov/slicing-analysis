package de.uni_passau.fim.se2.sa.slicing.output;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;

import java.util.*;

/** Sorts a set of nodes based on the {@link Node#getLineNumber()} value. */
public class NodeSorter {

  /** Prevent initialisation of utility class. */
  private NodeSorter() {}

  /**
   * Returns a collection of nodes, sorted based on the ascending order of the {@link
   * Node#getLineNumber()} value.
   *
   * @param pNodes A set of {@link Node}s
   * @return The sorted list
   */
  static List<Node> sort(Collection<Node> pNodes) {
    ListMultimap<Integer, Node> nodes = MultimapBuilder.treeKeys().arrayListValues().build();
    List<Node> result = new ArrayList<>();
    for (Node node : pNodes) {
      nodes.put(node.getLineNumber(), node);
    }
    for (Integer lineNumber : nodes.keySet()) {
      result.addAll(nodes.get(lineNumber));
    }
    return result;
  }

  public static List<Node> sortCFGOrder(ProgramGraph cfg) {
    List<Node> dominatorOrder = new ArrayList<>(sortPostDominatorOrder(cfg));
    Collections.reverse(dominatorOrder);
    return dominatorOrder;
  }

  public static List<Node> sortPostDominatorOrder(ProgramGraph reversedCFG) {
    List<Node> postOrder = new ArrayList<>();
    Set<Node> visited = new HashSet<>();
    Deque<Node> stack = new ArrayDeque<>();
    Node entry = reversedCFG.getEntry().orElseThrow();

    stack.push(entry);

    while (!stack.isEmpty()) {
      Node node = stack.pop();
      if (!visited.add(node)) {
        continue;
      }

      // Post-order insertion happens after visiting all successors,
      // so we temporarily push a marker after children.
      postOrder.add(node);

      for (Node successor : reversedCFG.getSuccessors(node)) {
        if (!visited.contains(successor)) {
          stack.push(successor);
        }
      }
    }

    return postOrder;
  }
}
