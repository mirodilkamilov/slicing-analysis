package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import org.objectweb.asm.*;

class InstrumentationAdapter extends ClassVisitor {

  InstrumentationAdapter(int pAPI, ClassWriter pClassWriter) {
    super(pAPI, pClassWriter);
  }

  @Override
  public MethodVisitor visitMethod(
      int pAccess, String pName, String pDescriptor, String pSignature, String[] pExceptions) {
    MethodVisitor mv = super.visitMethod(pAccess, pName, pDescriptor, pSignature, pExceptions);
    return new MethodVisitor(Opcodes.ASM9, mv) {
      @Override
      public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        mv.visitLdcInsn(pName);
        mv.visitLdcInsn(line);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "CoverageTracker",  // Instrumentation target class
                "trackLineVisit",
                "(Ljava/lang/String;I)V",
                false
        );
      }
    };
  }
}
