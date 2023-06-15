package de.uni_passau.fim.se2.sa.slicing.output;

import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import java.util.Collection;
import java.util.List;

public class XMLFileExtractor implements Extractor {

  private final Collection<Node> nodes;

  public XMLFileExtractor(Collection<Node> pNodes) {
    nodes = pNodes;
  }

  @Override
  public String extract() {
    List<Node> sortedNodes = NodeSorter.sort(nodes);
    StringBuilder builder = new StringBuilder();
    builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    builder.append("<report>\n");

    for (final Node node : sortedNodes) {
      String prettyPrint = node.prettyPrint().trim();
      String nodeID = node.getID();

      if (nodeID.equals("\"start\"")) {
        continue;
      }

      if (nodeID.matches("-?\\d+")) {
        builder.append(
            String.format(
                "  <line nr=\"%d\" id=\"%d\" instruction=\"%s\"/>%n",
                node.getLineNumber(), Integer.parseInt(nodeID), prettyPrint));
      } else {
        builder.append(
            String.format(
                "  <line nr=\"%d\" id=\"%s\" instruction=\"%s\"/>%n",
                node.getLineNumber(), nodeID, prettyPrint));
      }
    }

    builder.append("</report>\n");
    return builder.toString();
  }
}
