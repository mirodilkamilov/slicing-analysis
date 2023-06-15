package de.uni_passau.fim.se2.sa.slicing.cfg;

import java.util.LinkedHashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * A class visitor that extracts the information of the local variable table.
 *
 * @see LocalVariableTable
 * @see ClassVisitor
 */
public class CFGLocalVariableTableVisitor extends ClassVisitor {

  private final Map<String, LocalVariableTable> localVariableTables;

  public CFGLocalVariableTableVisitor(int pAPI) {
    super(pAPI);
    localVariableTables = new LinkedHashMap<>();
  }

  @Override
  public MethodVisitor visitMethod(
      int pAccess, String pName, String pDescriptor, String pSignature, String[] pExceptions) {
    MethodVisitor mv;
    if (cv != null) {
      mv = cv.visitMethod(pAccess, pName, pDescriptor, pSignature, pExceptions);
    } else {
      mv = null;
    }
    return new CFGLocalVariableTableMethodVisitor(
        this, api, mv, pName, pDescriptor, pSignature, pExceptions);
  }

  /**
   * Returns the information of the local variable tables for each method in a map of method name
   * and {@link LocalVariableTable}.
   *
   * @return A map of method name to {@link LocalVariableTable}
   */
  public Map<String, LocalVariableTable> getLocalVariableTables() {
    return localVariableTables;
  }

  private static class CFGLocalVariableTableMethodVisitor extends MethodVisitor {
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;
    private final LocalVariableTable localVariableTable;
    private final CFGLocalVariableTableVisitor classVisitor;

    CFGLocalVariableTableMethodVisitor(
        CFGLocalVariableTableVisitor pClassVisitor,
        int pAPI,
        MethodVisitor pMethodVisitor,
        String pName,
        String pDescriptor,
        String pSignature,
        String[] pExceptions) {
      super(pAPI, pMethodVisitor);
      classVisitor = pClassVisitor;
      name = pName;
      descriptor = pDescriptor;
      signature = pSignature;
      exceptions = pExceptions;
      localVariableTable = new LocalVariableTable();
    }

    /** {@inheritDoc} */
    @Override
    public void visitLocalVariable(
        String pName, String pDescriptor, String pSignature, Label pStart, Label pEnd, int pIndex) {
      localVariableTable.addEntry(
          pIndex, new LocalVariable(pName, pDescriptor, pSignature, pIndex));
      super.visitLocalVariable(pName, pDescriptor, pSignature, pStart, pEnd, pIndex);
    }

    /** {@inheritDoc} */
    @Override
    public void visitEnd() {
      String methodName =
          CFGExtractor.computeInternalMethodName(name, descriptor, signature, exceptions);
      classVisitor.localVariableTables.put(methodName, localVariableTable);
    }
  }
}
