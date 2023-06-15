package de.uni_passau.fim.se2.sa.slicing.cfg;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** Represents a node in the {@link ProgramGraph}. */
public class Node {

  private AbstractInsnNode instruction = null;
  private final int lineNumber;

  private static Map<Object, String> sIds = null;
  static int sNextId = 1;

  private final String id;

  /**
   * Creates a new node object.
   *
   * @param pInstructionNode The instruction node this node is based on
   * @param pLineNumber The line number in the source file
   */
  Node(AbstractInsnNode pInstructionNode, int pLineNumber) {
    instruction = pInstructionNode;
    lineNumber = pLineNumber;
    id = getID(pInstructionNode);
  }

  public Node(String pID) {
    id = "\"" + pID + "\"";
    lineNumber = -1;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Provides a pretty-printed version of this instruction.
   *
   * @return A string representation of this instruction
   */
  public String prettyPrint() {
    if (instruction == null) {
      return "";
    }

    Textifier textifier = new Textifier();
    MethodVisitor visitor = new TraceMethodVisitor(textifier);
    instruction.accept(visitor);
    StringWriter writer = new StringWriter();
    try (PrintWriter pw = new PrintWriter(writer)) {
      textifier.print(pw);
    }
    return writer.toString();
  }

  private static String getID(Object pObject) {
    if (sIds == null) {
      sIds = new LinkedHashMap<>();
    }

    return sIds.computeIfAbsent(pObject, k -> Integer.toString(sNextId++));
  }

  public String getID() {
    return id;
  }

  /**
   * Provides the instruction.
   *
   * @return The instruction
   */
  public AbstractInsnNode getInstruction() {
    return instruction;
  }

  @Override
  public String toString() {
    if (instruction == null) {
      return id;
    }
    StringBuilder builder = new StringBuilder();
    if (instruction instanceof LabelNode) {
      builder.append("LABEL");
    } else if (instruction instanceof LineNumberNode lineNumberNode) {
      builder.append("LINENUMBER ").append(lineNumberNode.line);
    } else if (instruction instanceof FrameNode) {
      builder.append("FRAME");
    } else {
      int opcode = instruction.getOpcode();
      String[] opcodes = Printer.OPCODES;
      if (opcode > 0 && opcode <= opcodes.length) {
        builder.append(opcodes[opcode]);
        if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
          builder.append("(").append(((MethodInsnNode) instruction).name).append(")");
        }
      }
    }
    builder.append(getID(instruction));
    builder.append("  ").append("line number: ").append(lineNumber);
    return "\"" + builder + "\"";
  }

  @Override
  public int hashCode() {
    return 31 + ((id == null) ? 0 : id.hashCode());
  }

  @Override
  public boolean equals(Object pOther) {
    if (this == pOther) {
      return true;
    }
    if (pOther == null || getClass() != pOther.getClass()) {
      return false;
    }

    Node other = (Node) pOther;
    if (id == null) {
      return other.id == null;
    } else {
      return id.equals(other.id);
    }
  }
}
