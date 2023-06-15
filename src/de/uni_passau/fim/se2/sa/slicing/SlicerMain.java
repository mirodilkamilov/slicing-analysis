package de.uni_passau.fim.se2.sa.slicing;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.slicing.agent.SlicerAgent;
import de.uni_passau.fim.se2.sa.slicing.cfg.CFGLocalVariableTableVisitor;
import de.uni_passau.fim.se2.sa.slicing.cfg.LocalVariable;
import de.uni_passau.fim.se2.sa.slicing.cfg.LocalVariableTable;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import de.uni_passau.fim.se2.sa.slicing.cfg.ProgramGraph;
import de.uni_passau.fim.se2.sa.slicing.coverage.CoverageTracker;
import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import de.uni_passau.fim.se2.sa.slicing.output.ByteCodeExtractor;
import de.uni_passau.fim.se2.sa.slicing.output.Extractor;
import de.uni_passau.fim.se2.sa.slicing.output.SourceLineExtractor;
import de.uni_passau.fim.se2.sa.slicing.output.XMLFileExtractor;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

public final class SlicerMain implements Callable<Integer> {

  @Spec CommandSpec spec;

  private String className;
  private String methodName;
  private String methodDescriptor;
  private String variableName;
  private int lineNumber;
  private Path sourceFilePath;
  private Path targetFilePath;
  private boolean xmlExtraction;
  private Map<String, LocalVariableTable> localVariableTables;
  private MethodNode methodNode;
  private boolean dynamicSlicing;
  private String testCase;

  public static void main(String[] pArgs) {
    new CommandLine(new SlicerMain()).execute(pArgs);
  }

  private SlicerMain() {}

  @Override
  public Integer call() throws Exception {
    run();
    return 0;
  }

  private void run() throws IOException {
    if (dynamicSlicing) {
      if (!SlicerAgent.wasInvoked()) {
        throw new IllegalStateException(
            "The SlicerAgent must have been invoked for dynamic slicing");
      }

      SlicerUtil.executeTest(className, testCase);
    }

    final Set<Node> slice = executeSlicing();

    final Extractor extractor;
    if (sourceFilePath != null) {
      extractor =
          new SourceLineExtractor(
              sourceFilePath, localVariableTables, className, methodNode, slice);
    } else if (xmlExtraction) {
      extractor = new XMLFileExtractor(slice);
    } else {
      extractor = new ByteCodeExtractor(slice);
    }

    if (targetFilePath == null) {
      System.out.println(extractor.extract());
    } else {
      extractor.extractToFile(targetFilePath);
    }
  }

  private Set<Node> executeSlicing() throws IOException {
    final int apiLevel = Opcodes.ASM9;
    final ClassNode classNode = new ClassNode(apiLevel);
    final ClassReader classReader = new ClassReader(className);
    classReader.accept(classNode, 0);

    final CFGLocalVariableTableVisitor visitor = new CFGLocalVariableTableVisitor(apiLevel);
    classReader.accept(visitor, 0);
    localVariableTables = visitor.getLocalVariableTables();

    methodNode =
        classNode.methods.stream()
            .filter(m -> methodName.equals(m.name) && methodDescriptor.equals(m.desc))
            .findAny()
            .orElse(null);
    Preconditions.checkNotNull(methodNode, "Could not find an appropriate method!");

    ProgramDependenceGraph pdg = new ProgramDependenceGraph(classNode, methodNode);
    final Node programLocation =
        getProgramLocation(
            pdg.getCFG(),
            methodNode,
            localVariableTables.get(methodNode.name + ": " + methodNode.desc),
            lineNumber,
            variableName);

    if (dynamicSlicing) {
      pdg = SlicerUtil.simplify(pdg);
    }

    return pdg.backwardSlice(programLocation);
  }

  private Node getProgramLocation(
      final ProgramGraph pCFG,
      final MethodNode pMethodNode,
      final LocalVariableTable pLocalVariableTable,
      final int pLineNumber,
      final String pVariableName) {
    // Search for the instruction that is located at the given line number.
    AbstractInsnNode targetInstruction = null;
    for (final AbstractInsnNode insnNode : methodNode.instructions) {
      if (insnNode instanceof LineNumberNode lineNumberNode) {
        if (lineNumberNode.line == pLineNumber) {
          targetInstruction = insnNode;
          break;
        }
      }
    }

    // Search for the node that contains the instruction.
    Node cfgNode = null;
    if (targetInstruction != null) {
      for (final Node node : pCFG.getNodes()) {
        if (node.getInstruction().equals(targetInstruction)) {
          cfgNode = node;
          break;
        }
      }
    }

    for (final Node successor : pCFG.getSuccessorsUntilNextLineNumber(cfgNode)) {
      if ((successor.getInstruction() instanceof VarInsnNode
              || successor.getInstruction() instanceof IincInsnNode)
          && isStoreOpCode(successor.getInstruction().getOpcode())) {
        // For a local variable search in the local variable table if we find a candidate
        final int idx;
        if (successor.getInstruction() instanceof VarInsnNode varInsnNode) {
          idx = varInsnNode.var;
        } else {
          idx = ((IincInsnNode) successor.getInstruction()).var;
        }

        final Optional<LocalVariable> entry = pLocalVariableTable.getEntry(idx);
        if (entry.isPresent() && entry.get().name().equals(variableName)) {
          return successor;
        }
      } else if (successor.getInstruction() instanceof FieldInsnNode fieldInsnNode
          && isFieldOpCode(successor.getInstruction().getOpcode())) {
        // For a field access compare the name directly
        final String instructionVariableName = fieldInsnNode.name;
        if (instructionVariableName != null && instructionVariableName.equals(variableName)) {
          return successor;
        }
      }
    }

    throw new IllegalStateException(
        "We were not able to determine a correct program location for the searched node.");
  }

  private boolean isStoreOpCode(final int pOpCode) {
    return pOpCode == Opcodes.AASTORE
        || pOpCode == Opcodes.BASTORE
        || pOpCode == Opcodes.CASTORE
        || pOpCode == Opcodes.DASTORE
        || pOpCode == Opcodes.FASTORE
        || pOpCode == Opcodes.IASTORE
        || pOpCode == Opcodes.LASTORE
        || pOpCode == Opcodes.SASTORE
        || pOpCode == Opcodes.ASTORE
        || pOpCode == Opcodes.DSTORE
        || pOpCode == Opcodes.FSTORE
        || pOpCode == Opcodes.ISTORE
        || pOpCode == Opcodes.LSTORE
        || pOpCode == Opcodes.IINC;
  }

  private boolean isFieldOpCode(final int pOpCode) {
    return pOpCode == Opcodes.PUTFIELD || pOpCode == Opcodes.PUTSTATIC;
  }

  // @formatter:off
  @Option(
      names = {"-c", "--class"},
      required = true,
      description = "The class that contains the method to slice.")
  // @formatter:on
  public void setClassName(final String pClassName) {
    className = pClassName;
  }

  // @formatter:off
  @Option(
      names = {"-v", "--variablename"},
      required = true,
      description = "Name of the variable to slice.")
  // @formatter:on
  public void setVariableName(final String pVariableName) {
    variableName = pVariableName;
  }

  // @formatter:off
  @Option(
      names = {"-m", "--method"},
      required = true,
      description = "The method to slice. Requires <methodname>:<descriptor> syntax.")
  // @formatter:on
  public void setMethod(final String pMethod) {
    final String[] method = pMethod.split(":");
    methodName = method[0].replace("\"", "");
    methodDescriptor = method[1].replace("\"", "");
  }

  // @formatter:off
  @Option(
      names = {"-l", "--linenumber"},
      required = true,
      description = "The line number of the variable to slice.")
  // @formatter:on
  public void setLineNumber(final int pLineNumber) {
    lineNumber = pLineNumber;
  }

  // @formatter:off
  @Option(
      names = {"-s", "--sourcefile"},
      description = "The path to the class file's source code.")
  // @formatter:on
  public void setSourceFilePath(final Path pSourceFilePath) {
    sourceFilePath = pSourceFilePath;
  }

  // @formatter:off
  @Option(
      names = {"-t", "--targetfile"},
      description = "Path to a target file where to write the sliced code to")
  // @formatter:on
  public void setTargetFilePath(final Path pTargetFilePath) {
    targetFilePath = pTargetFilePath;
  }

  // @formatter:off
  @Option(
      names = {"-x", "--xmlfile"},
      description = "Extracts the result as an XML file for grading")
  // @formatter:on
  public void setXmlExtraction(final boolean pXmlExtraction) {
    xmlExtraction = pXmlExtraction;
  }

  // @formatter:off
  @Option(
      names = {"-d", "--dynamic"},
      description = "Create a dynamic slice by executing the given test")
  // @formatter:on
  public void setDynamicSlicing(final String pTestCase) {
    dynamicSlicing = true;
    testCase = pTestCase;
  }
}
