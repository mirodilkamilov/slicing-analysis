package de.uni_passau.fim.se2.sa.slicing.graph;

import br.usp.each.saeg.asm.defuse.Variable;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import java.util.*;

public class DataDependenceGraph extends Graph {

  DataDependenceGraph(ClassNode pClassNode, MethodNode pMethodNode) {
    super(pClassNode, pMethodNode);
  }

  /**
   * Computes the data-dependence graph from the control-flow graph.
   *
   * <p>This requires the computation of the reaching-definition algorithm. We recommend using the
   * provided {@link DataFlowAnalysis} implementation.
   *
   * <p>Remember that the CFG stores for each node the instruction at that node. With that, calling
   * {@link DataFlowAnalysis#definedBy(String, MethodNode, AbstractInsnNode)} provides a collection
   * of {@link Variable}s that are defined by this particular instruction; calling {@link
   * DataFlowAnalysis#usedBy(String, MethodNode, AbstractInsnNode)} provides a collection of {@link
   * Variable}s that are used by this particular instruction, respectively. From this information
   * you can compute for each node n in the CFG the GEN[n] and KILL[n] sets. Afterwards, it is
   * possible to compute the IN[n] and OUT[n] sets using the reaching-definitions algorithm.
   *
   * <p>Finally, you can compute all def-use pairs and construct the data-dependence graph from
   * these pairs.
   *
   * @return The data-dependence graph for a control-flow graph
   */
  @Override
  public ProgramGraph computeResult() {
    // Step 1: Compute GEN and KILL sets
    Map<Node, Set<Definition>> GEN = new HashMap<>();
    Map<Node, Set<Definition>> KILL = new HashMap<>();

    Collection<Node> allNodes = cfg.getNodes();
    for (Node node : allNodes) {
      AbstractInsnNode insn = node.getInstruction();
      try {
        Set<Variable> defs = new HashSet<>(DataFlowAnalysis.definedBy(this.classNode.name, this.methodNode, insn));
        if (!defs.isEmpty()) {
          Set<Definition> temp = new HashSet<>();
          defs.forEach(def -> temp.add(new Definition(node, def)));
          GEN.put(node, temp);
        }

        // KILL[n] is all other definitions of the same variable in the entire method
        Set<Definition> killed = new HashSet<>();
        if (GEN.get(node) == null || GEN.get(node).isEmpty()) {
          continue;
        }
        for (Node otherNode : allNodes) {
          AbstractInsnNode otherInsn = otherNode.getInstruction();
          if (otherNode == node || otherInsn == null) continue;
          Collection<Variable> otherDefs = DataFlowAnalysis.definedBy(this.classNode.name, this.methodNode, otherInsn);
          for (Variable var : otherDefs) {
            if (defs.contains(var)) {
              killed.add(new Definition(otherNode, var));
            }
          }
        }
        if (!killed.isEmpty()) {
          KILL.put(node, killed);
        }
      } catch (AnalyzerException e) {
        throw new RuntimeException("Error analyzing instruction: " + insn, e);
      }
    }

    // Step 2: Reaching Definitions with Iterative Algorithm
    Map<Node, Set<Definition>> IN = new HashMap<>();
    Map<Node, Set<Definition>> OUT = new HashMap<>();

    for (Node n : allNodes) {
      OUT.put(n, new HashSet<>(GEN.getOrDefault(n, Collections.emptySet())));
      IN.put(n, new HashSet<>());
    }

    boolean changed;
    do {
      changed = false;

      for (Node n : allNodes) {
        Set<Definition> oldIn = IN.get(n);
        Set<Definition> oldOut = OUT.get(n);

        // IN[n] = U OUT[p] for all predecessors p of n
        List<Node> predecessors = new ArrayList<>(
                cfg.getPredecessors(n)
        );
        Set<Definition> newIn = new HashSet<>();
        for (Node pred : predecessors) {
          newIn.addAll(OUT.getOrDefault(pred, Set.of()));
        }
        IN.put(n, newIn);

        // OUT[n] = GEN[n] ∪ (IN[n] − KILL[n])
        Set<Definition> newOut = new HashSet<>(GEN.getOrDefault(n, Set.of()));
        Set<Definition> inMinusKill = new HashSet<>(newIn);
        inMinusKill.removeAll(KILL.getOrDefault(n, Set.of()));
        newOut.addAll(inMinusKill);
        OUT.put(n, newOut);

        if (!oldIn.equals(newIn) || !oldOut.equals(newOut)) {
          changed = true;
        }
      }

    } while (changed);

    // Step 3: Add edges for def-use pairs to build DDG
    ProgramGraph pDDG = new ProgramGraph();
    for (Node n : allNodes) {
      AbstractInsnNode insn = n.getInstruction();
      try {
        Collection<Variable> usedVars = DataFlowAnalysis.usedBy(this.classNode.name, this.methodNode, insn);
        for (Variable v : usedVars) {
          for (Definition def : IN.getOrDefault(n, Set.of())) {
            if (def.variable().equals(v)) {
              pDDG.addNode(def.node());
              pDDG.addNode(n);
              pDDG.addEdge(def.node(), n);
            }
          }
        }
      } catch (AnalyzerException e) {
        throw new RuntimeException("Error analyzing used variables at: " + insn, e);
      }
    }

    return pDDG;
  }
}
