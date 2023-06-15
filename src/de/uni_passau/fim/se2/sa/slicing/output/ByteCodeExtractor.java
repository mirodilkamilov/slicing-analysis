package de.uni_passau.fim.se2.sa.slicing.output;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import java.util.Collection;
import java.util.List;

public class ByteCodeExtractor implements Extractor {

  private final Collection<Node> nodes;

  public ByteCodeExtractor(Collection<Node> pNodes) {
    nodes = pNodes;
  }

  /** {@inheritDoc} */
  @Override
  public String extract() {
    List<Node> sortedNodes = NodeSorter.sort(nodes);
    StringBuilder builder = new StringBuilder();

    for (Node node : sortedNodes) {
      String prettyPrint = node.prettyPrint().trim();
      String nodeID = node.getID();

      if (nodeID.equals("\"start\"")) {
        continue; // skip start node
      }

      if (nodeID.matches("-?\\d+")) {
        builder.append(
            String.format(
                "(line: %4d, id: %4d)  %s%n",
                node.getLineNumber(), Integer.parseInt(nodeID), prettyPrint));
      } else {
        builder.append(
            String.format("(line: %4d, id: %s)  %s%n", node.getLineNumber(), nodeID, prettyPrint));
      }
    }
    return builder.toString();
  }
}
