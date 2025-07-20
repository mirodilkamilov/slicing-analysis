package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import org.objectweb.asm.*;

class InstrumentationAdapter extends ClassVisitor {

  private final String className;

  InstrumentationAdapter(int pAPI, ClassWriter pClassWriter, String className) {
    super(pAPI, pClassWriter);
    this.className = className;
  }

  @Override
  public MethodVisitor visitMethod(
      int pAccess, String pName, String pDescriptor, String pSignature, String[] pExceptions) {
    MethodVisitor mv = super.visitMethod(pAccess, pName, pDescriptor, pSignature, pExceptions);
    return new MethodVisitor(Opcodes.ASM9, mv) {
      @Override
      public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        mv.visitLdcInsn(line);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "CoverageTracker",  // Instrumentation target class
                "trackLineVisit",
                "(I)V",
                false
        );
      }
    };
  }
}
