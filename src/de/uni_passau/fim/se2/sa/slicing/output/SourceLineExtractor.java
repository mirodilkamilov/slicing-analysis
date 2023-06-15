package de.uni_passau.fim.se2.sa.slicing.output;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Var;
import de.uni_passau.fim.se2.sa.slicing.cfg.LocalVariable;
import de.uni_passau.fim.se2.sa.slicing.cfg.LocalVariableTable;
import de.uni_passau.fim.se2.sa.slicing.cfg.Node;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class SourceLineExtractor implements Extractor {

  private final Path sourceFilePath;
  private final Map<String, LocalVariableTable> localVariableTables;
  private final String className;
  private final MethodNode methodNode;
  private final Set<Node> nodes;

  public SourceLineExtractor(
      Path pSourceFilePath,
      Map<String, LocalVariableTable> pLocalVariableTables,
      String pClassName,
      MethodNode pMethodNode,
      Set<Node> pNodes) {
    sourceFilePath = pSourceFilePath;
    localVariableTables = pLocalVariableTables;
    className = pClassName;
    methodNode = pMethodNode;
    nodes = pNodes;
  }

  @Override
  public String extract() throws IOException {
    List<String> lines = new ArrayList<>();
    try (Stream<String> fileLines = Files.lines(sourceFilePath, Charset.defaultCharset())) {
      fileLines.forEach(lines::add);
    }

    List<Node> sortedNodes = NodeSorter.sort(nodes);
    StringBuilder builder = new StringBuilder();

    // if the first or second node is a LabelNode, it refers to a dependency on one of the parameter
    // variables
    if (sortedNodes.size() > 1
        && (sortedNodes.get(1).getInstruction() instanceof LabelNode
            || sortedNodes.get(0).getInstruction() instanceof LabelNode)) {
      builder.append(generateMethodDeclaration()).append("\n");
    }

    @Var int lastLineNUmber = Integer.MIN_VALUE;
    for (final Node node : sortedNodes) {
      int lineNumber = node.getLineNumber();
      if (lineNumber > 1 && lineNumber != lastLineNUmber) {
        String line = lines.get(lineNumber - 1).trim();
        builder.append(line).append("\n");
      }
      lastLineNUmber = lineNumber;
    }

    return builder.toString();
  }

  private String generateMethodDeclaration() {
    LocalVariableTable localVariableTable =
        localVariableTables.get(methodNode.name + ": " + methodNode.desc);
    Optional<LocalVariable> optionalFirstVariable = localVariableTable.getEntry(0);
    if (optionalFirstVariable.isEmpty()) {
      return "";
    }

    LocalVariable firstVariable = optionalFirstVariable.get();
    boolean isVirtual =
        firstVariable.name().equals("this")
            && firstVariable.descriptor().equals("L" + className.replace(".", "/") + ";");
    String modifiers = getModifiers();

    Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);
    String[] argumentTypeStrings = new String[argumentTypes.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      argumentTypeStrings[i] =
          replaceDescriptor(argumentTypes[i].toString())
              + " "
              + lookupLocalVariableName(argumentTypes[i], i, localVariableTable, isVirtual);
    }

    Type returnType = Type.getReturnType(methodNode.desc);
    String returnTypeString = replaceDescriptor(returnType.toString());

    String arguments = String.join(", ", argumentTypeStrings);

    return String.format("%s%s %s(%s) {", modifiers, returnTypeString, methodNode.name, arguments);
  }

  private String lookupLocalVariableName(
      Type pArgumentType, int pIndex, LocalVariableTable pLocalVariableTable, boolean pIsVirtual) {
    Optional<LocalVariable> optionalVariable;
    if (pIsVirtual) {
      optionalVariable = pLocalVariableTable.getEntry(pIndex + 1);
    } else {
      optionalVariable = pLocalVariableTable.getEntry(pIndex);
    }

    Preconditions.checkState(optionalVariable.isPresent());
    LocalVariable variable = optionalVariable.get();
    Preconditions.checkState(pArgumentType.toString().equals(variable.descriptor()));
    return variable.name();
  }

  private String replaceDescriptor(String pDescriptor) {
    String descriptor = pDescriptor.replace("[", "");
    int arrayLevel = pDescriptor.length() - descriptor.length();
    StringBuilder builder = new StringBuilder();
    builder.append(
        switch (descriptor) {
          case "B" -> "byte";
          case "C" -> "char";
          case "D" -> "double";
          case "F" -> "float";
          case "I" -> "int";
          case "J" -> "long";
          case "S" -> "short";
          case "Z" -> "boolean";
          case "V" -> "void";
          default -> {
            final String[] parts = descriptor.split("/");
            yield parts[parts.length - 1].replace(";", "");
          }
        });
    builder.append("[]".repeat(Math.max(0, arrayLevel)));
    return builder.toString();
  }

  private String getModifiers() {
    int access = methodNode.access;
    StringBuilder builder = new StringBuilder();
    if ((access & Opcodes.ACC_PUBLIC) != 0) {
      builder.append("public ");
    }
    if ((access & Opcodes.ACC_PROTECTED) != 0) {
      builder.append("protected ");
    }
    if ((access & Opcodes.ACC_PRIVATE) != 0) {
      builder.append("private ");
    }
    if ((access & Opcodes.ACC_STATIC) != 0) {
      builder.append("static ");
    }
    if ((access & Opcodes.ACC_FINAL) != 0) {
      builder.append("final ");
    }
    if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
      builder.append("synchronized ");
    }
    if ((access & Opcodes.ACC_VOLATILE) != 0) {
      builder.append("volatile ");
    }
    if ((access & Opcodes.ACC_TRANSIENT) != 0) {
      builder.append("transient ");
    }
    if ((access & Opcodes.ACC_ABSTRACT) != 0) {
      builder.append("abstract ");
    }
    if ((access & Opcodes.ACC_STRICT) != 0) {
      builder.append("strictfp");
    }
    if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      builder.append("synthetic ");
    }
    if ((access & Opcodes.ACC_MANDATED) != 0) {
      builder.append("mandated ");
    }
    if ((access & Opcodes.ACC_ENUM) != 0) {
      builder.append("enum ");
    }
    if ((access & Opcodes.ACC_RECORD) != 0) {
      builder.append("record ");
    }
    return builder.toString();
  }
}
