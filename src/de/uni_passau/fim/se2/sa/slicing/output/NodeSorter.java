package de.uni_passau.fim.se2.sa.slicing.output;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Sorts a set of nodes based on the {@link Node#getLineNumber()} value. */
class NodeSorter {

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
}
