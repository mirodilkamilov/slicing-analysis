package de.uni_passau.fim.se2.sa.slicing.graph;

import java.util.Set;

/**
 * A interface defining a sliceable graph.
 *
 * @param <T> the type of the nodes in the graph
 */
public interface Sliceable<T> {

  /**
   * Computes the backward slice of the graph with the given criterion.
   *
   * @param pCriterion The slicing criterion
   * @return A set of nodes that are in the backward slice
   */
  Set<T> backwardSlice(T pCriterion);
}
