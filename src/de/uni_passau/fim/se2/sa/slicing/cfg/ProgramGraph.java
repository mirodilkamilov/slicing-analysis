package de.uni_passau.fim.se2.sa.slicing.cfg;

import com.google.errorprone.annotations.Var;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.objectweb.asm.tree.LineNumberNode;

/** Represents a graph structure. */
public class ProgramGraph {

  // A facade class to store graphs as DirectedMultiGraphs using the JGraphT framework.
  private final Graph<Node, DefaultEdge> graph;

  public ProgramGraph() {
    Node.sNextId = 1;
    graph =
        GraphTypeBuilder.<Node, DefaultEdge>directed()
            .allowingSelfLoops(true)
            .allowingMultipleEdges(true)
            .weighted(false)
            .edgeClass(DefaultEdge.class)
            .buildGraph();
  }

  /**
   * Adds a node to the graph.
   *
   * @param pNode The node to add
   */
  public void addNode(Node pNode) {
    graph.addVertex(pNode);
  }

  /**
   * Adds a directed edge between two {@link Node}s to the graph.
   *
   * @param pStartNode The start node of the edge
   * @param pEndNode The end node of the edge
   */
  public void addEdge(Node pStartNode, Node pEndNode) {
    graph.addEdge(pStartNode, pEndNode);
  }

  /**
   * Returns the immediate predecessors of a node.
   *
   * @param pNode The node whose predecessors we are searching for
   * @return A collection of nodes that are predecessors of the given node
   */
  public Collection<Node> getPredecessors(Node pNode) {
    Set<Node> predecessors = new LinkedHashSet<>();
    for (DefaultEdge edge : graph.incomingEdgesOf(pNode)) {
      predecessors.add(graph.getEdgeSource(edge));
    }
    return Collections.unmodifiableCollection(predecessors);
  }

  /**
   * Returns the immediate successors of a node.
   *
   * @param pNode The node whose successors we are searching for
   * @return A collection of nodes that are successors of the given node
   */
  public Collection<Node> getSuccessors(Node pNode) {
    if (!graph.containsVertex(pNode)) {
      return Set.of();
    }
    Set<Node> successors = new LinkedHashSet<>();
    for (DefaultEdge edge : graph.outgoingEdgesOf(pNode)) {
      successors.add(graph.getEdgeTarget(edge));
    }
    return Collections.unmodifiableCollection(successors);
  }

  /**
   * Returns all the nodes in the graph.
   *
   * @return A collection of all {@link Node}s in the graph
   */
  public Collection<Node> getNodes() {
    return graph.vertexSet();
  }

  /**
   * Provides the entry node—the node with no predecessors.
   *
   * <p>Assumes that there is only one such node in the graph.
   *
   * @return The entry {@link Node} of the graph
   */
  public Optional<Node> getEntry() {
    return graph.vertexSet().stream().filter(n -> graph.incomingEdgesOf(n).isEmpty()).findFirst();
  }

  /**
   * Provides the exit node—the node with no successors.
   *
   * <p>Assumes that there is only one such node in the graph.
   *
   * @return The exit {@link Node} of the graph
   */
  public Optional<Node> getExit() {
    return graph.vertexSet().stream().filter(n -> graph.outgoingEdgesOf(n).isEmpty()).findFirst();
  }

  /**
   * Provides a representation of the graph in the GraphViz DOT format.
   *
   * <p>Can be used to visualize the graph using GraphViz.
   *
   * @return A string representation of the graph in the GraphViz DOT format
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph ProgramGraph {\n");
    for (Node node : getNodes()) {
      for (Node successor : getSuccessors(node)) {
        sb.append(node.toString()).append("->").append(successor.toString()).append("\n");
      }
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Provides all transitive successors of a node.
   *
   * <p>Transitive successors are all nodes that can be reached from the given node by following
   * edges. This includes any instruction that could eventually be reached from the given node.
   *
   * @param pNode The {@link Node} whose transitive successors we are searching for
   * @return A collection of {@link Node}s that are transitive successors of the given node
   */
  public Collection<Node> getTransitiveSuccessors(Node pNode) {
    return transitiveSuccessors(pNode, new LinkedHashSet<>());
  }

  private Collection<Node> transitiveSuccessors(Node pNode, Set<Node> pDoneSet) {
    Collection<Node> successors = new LinkedHashSet<>();
    for (Node node : getSuccessors(pNode)) {
      if (!pDoneSet.contains(node)) {
        successors.add(node);
        pDoneSet.add(node);
        successors.addAll(transitiveSuccessors(node, pDoneSet));
      }
    }
    return successors;
  }

  /**
   * Searches the {@link ProgramGraph} for successors of a {@link Node} until the next node
   * containing a {@link LineNumberNode} is found.
   *
   * @param pNode The {@link Node} whose successors we are searching for
   * @return A collection of nodes that follow the {@code pNode} until a {@link LineNumberNode} is
   *     found
   */
  public Collection<Node> getSuccessorsUntilNextLineNumber(Node pNode) {
    Collection<Node> successors = new LinkedHashSet<>();
    Queue<Node> waitList = new ArrayDeque<>();
    waitList.offer(pNode);

    while (!waitList.isEmpty()) {
      Node current = waitList.poll();
      for (Node successor : getSuccessors(current)) {
        if (successor.getInstruction() instanceof LineNumberNode) {
          continue;
        }
        successors.add(successor);
        waitList.offer(successor);
      }
    }

    return successors;
  }

  /**
   * For a given pair of nodes in a directed acyclic graph, return the ancestor that is common to
   * both nodes.
   *
   * <p><em>Important:</em> This method assumes that the graph is a directed acyclic graph (DAG).
   *
   * @param pFirstNode A {@link Node}
   * @param pSecondNode A {@link Node}
   * @return The node that is the least common ancestor of the two parameter nodes
   */
  public Node getLeastCommonAncestor(Node pFirstNode, Node pSecondNode) {
    @Var Node current = pFirstNode;
    while (!containsTransitiveSuccessors(current, pFirstNode, pSecondNode)) {
      current = getPredecessors(current).iterator().next();
    }
    return current;
  }

  private boolean containsTransitiveSuccessors(Node pStartNode, Node pFirstNode, Node pSecondNode) {
    Collection<Node> transitiveSuccessors = getTransitiveSuccessors(pStartNode);
    transitiveSuccessors.add(pStartNode);
    return transitiveSuccessors.contains(pFirstNode) && transitiveSuccessors.contains(pSecondNode);
  }
}
