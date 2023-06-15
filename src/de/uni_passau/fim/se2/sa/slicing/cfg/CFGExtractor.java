package de.uni_passau.fim.se2.sa.slicing.cfg;

import com.google.errorprone.annotations.Var;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

public class CFGExtractor {

  private CFGExtractor() {}

  /**
   * Builds the control-flow graph for a given method.
   *
   * @param pOwningClass The name of the class that owns the method
   * @param pMethodNode The ASM {@link MethodNode} representing the method
   * @return The control-flow graph for the given method
   * @throws AnalyzerException In case of problems during the analysis of the method
   */
  public static ProgramGraph buildCFG(String pOwningClass, MethodNode pMethodNode)
      throws AnalyzerException {
    ProgramGraph cfg = buildGraph(pOwningClass, pMethodNode);

    // Create distinguished entry and exit nodes.
    Node entry = new Node("Entry");
    Node exit = new Node("Exit");
    cfg.addNode(entry);
    cfg.addNode(exit);

    // Connect entry and exit nodes to the other nodes of the graph.
    for (Node node : cfg.getNodes()) {
      if (node.toString().equals("\"Exit\"") || node.toString().equals("\"Entry\"")) {
        continue; // Skip the entry or exit nodes themselves.
      }

      // We connect every node that does not have an outgoing connection to the exit node.
      if (cfg.getSuccessors(node).isEmpty()) {
        cfg.addEdge(node, exit);
      }

      // We connect the entry node to every node that does not have an incoming connection.
      if (cfg.getPredecessors(node).isEmpty()) {
        cfg.addEdge(entry, node);
      }
    }

    return cfg;
  }

  private static ProgramGraph buildGraph(String pOwningClass, MethodNode pMethodNode)
      throws AnalyzerException {
    InsnList instructions = pMethodNode.instructions;
    Map<AbstractInsnNode, Node> nodes = new LinkedHashMap<>();
    ProgramGraph cfg = new ProgramGraph();
    Analyzer<BasicValue> analyzer =
        new Analyzer<>(new BasicInterpreter()) {

          @Override
          protected void newControlFlowEdge(int pSourceIndex, int pTargetIndex) {
            AbstractInsnNode source = instructions.get(pSourceIndex);
            AbstractInsnNode target = instructions.get(pTargetIndex);
            int sourceLineNumber = findLineNumber(instructions, pSourceIndex);
            int targetLineNumber = findLineNumber(instructions, pTargetIndex);

            // Add the source node to the CFG if it does not exist yet.
            @Var Node sourceNode = nodes.get(source);
            if (sourceNode == null) {
              sourceNode = new Node(source, sourceLineNumber);
              nodes.put(source, sourceNode);
              cfg.addNode(sourceNode);
            }

            // Add the target node to the CFG if it does not exist yet.
            @Var Node targetNode = nodes.get(target);
            if (targetNode == null) {
              targetNode = new Node(target, targetLineNumber);
              nodes.put(target, targetNode);
              cfg.addNode(targetNode);
            }

            // Finally, establish the connection between source and target node.
            cfg.addEdge(sourceNode, targetNode);
          }

          private int findLineNumber(InsnList pInstructions, int pStartPoint) {
            // ASM also encodes meta-information about instructions in the instructions list, e.g.,
            // via LineNumberNodes.  Starting at the instruction of interest, we traverse the list
            // of
            // instructions in reverse order until we find such a LineNumberNode.
            for (int i = pStartPoint; i >= 0; --i) {
              @Var AbstractInsnNode current = pInstructions.get(i);
              if (current instanceof LineNumberNode lineNumberNode) {
                return lineNumberNode.line;
              }
            }
            return -1; // No line number found.
          }
        };

    // The analyzer already builds the CFG internally.  We can extract it from there.
    analyzer.analyze(pOwningClass, pMethodNode);

    return cfg;
  }

  /**
   * Computes the internal name representation of a method.
   *
   * @param pMethodNode The method node
   * @return The internal name representation of the method
   */
  static String computeInternalMethodName(MethodNode pMethodNode) {
    String methodName = pMethodNode.name;
    String descriptor = pMethodNode.desc;
    String signature = pMethodNode.signature;
    String[] exceptions;
    if (pMethodNode.exceptions.isEmpty()) {
      exceptions = null;
    } else {
      exceptions = pMethodNode.exceptions.toArray(new String[0]);
    }
    return computeInternalMethodName(methodName, descriptor, signature, exceptions);
  }

  /**
   * Computes the internal name representation of a method.
   *
   * @param pMethodName The name of the method
   * @param pDescriptor The descriptor of the method
   * @param pSignature The signature of the method
   * @param pExceptions The exceptions thrown by the method
   * @return The internal name representation of the method
   */
  static String computeInternalMethodName(
      String pMethodName, String pDescriptor, String pSignature, String[] pExceptions) {
    StringBuilder result = new StringBuilder();
    result.append(pMethodName);
    result.append(": ");
    result.append(pDescriptor);
    if (pSignature != null) {
      result.append("; ").append(pSignature);
    }
    if (pExceptions != null) {
      result.append("; ").append(Arrays.toString(pExceptions));
    }
    return result.toString();
  }
}
