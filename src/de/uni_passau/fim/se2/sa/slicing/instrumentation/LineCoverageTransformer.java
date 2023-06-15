package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class LineCoverageTransformer implements ClassFileTransformer {

  private final String instrumentationTarget;

  public LineCoverageTransformer(String pInstrumentationTarget) {
    instrumentationTarget = pInstrumentationTarget.replace('.', '/');
  }

  @Override
  public byte[] transform(
      ClassLoader pClassLoader,
      String pClassName,
      Class<?> pClassBeingRedefined,
      ProtectionDomain pProtectionDomain,
      byte[] pClassFileBuffer) {
    if (isIgnored(pClassName)) {
      return pClassFileBuffer;
    }

    // TODO Implement me
    throw new UnsupportedOperationException("Implement me");
  }

  private boolean isIgnored(String pClassName) {
    return !pClassName.startsWith(instrumentationTarget) || pClassName.endsWith("Test");
  }
}
