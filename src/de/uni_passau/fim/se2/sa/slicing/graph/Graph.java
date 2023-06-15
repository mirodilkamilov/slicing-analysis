package de.uni_passau.fim.se2.sa.slicing.graph;

import com.google.errorprone.annotations.Var;
import de.uni_passau.fim.se2.sa.slicing.cfg.CFGExtractor;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/** An abstract based class for analyses. */
public abstract class Graph {

  protected final ProgramGraph cfg;
  protected final MethodNode methodNode;
  protected final ClassNode classNode;

  protected Graph(ClassNode pClassNode, MethodNode pMethodNode) {
    @Var ProgramGraph graph = null;
    if (pClassNode == null) {
      // This should only happen under testing conditions.
      classNode = null;
      methodNode = null;
      cfg = null;
      return;
    }

    try {
      graph = CFGExtractor.buildCFG(pClassNode.name, pMethodNode);
    } catch (AnalyzerException e) {
      e.printStackTrace(); // ugly but should not happen anyway
    }

    cfg = graph;
    methodNode = pMethodNode;
    classNode = pClassNode;
  }

  protected Graph(ProgramGraph pCFG) {
    cfg = pCFG;
    methodNode = null;
    classNode = null;
  }

  /**
   * Returns the program graph of the method.
   *
   * @return the program graph of the method
   */
  public ProgramGraph getCFG() {
    return cfg;
  }

  /**
   * Computes the graph transformation and returns a new {@link ProgramGraph} of the result.
   *
   * <p>This method needs to be implemented by the concrete analysis. It has to create a new {@link
   * ProgramGraph} object!
   *
   * @return A new {@link ProgramGraph} of the result.
   */
  public abstract ProgramGraph computeResult();

  /**
   * Computes the reverse graph of the given graph.
   *
   * <p>Creates a new {@link ProgramGraph} object.
   *
   * @param pGraph The graph to reverse
   * @return The reverse graph
   */
  protected ProgramGraph reverseGraph(ProgramGraph pGraph) {
    // TODO Implement me
    throw new UnsupportedOperationException("Implement me");
  }
}
