package de.uni_passau.fim.se2.sa.slicing.agent;

import de.uni_passau.fim.se2.sa.slicing.instrumentation.LineCoverageTransformer;
import java.lang.instrument.Instrumentation;

public final class SlicerAgent {

  private static boolean invoked = false;
  private static final String DEFAULT_PACKAGE = "de.uni_passau.fim.se2.sa.examples";

  private SlicerAgent() {}

  /**
   * Entry point for the agent.
   *
   * @param pPackageToInstrument The package name to instrument
   * @param pInstrumentation The instrumentation instance
   */
  public static void premain(String pPackageToInstrument, Instrumentation pInstrumentation) {
    if (!invoked) {
      String target =
          pPackageToInstrument != null && !pPackageToInstrument.isBlank()
              ? pPackageToInstrument
              : DEFAULT_PACKAGE;
      pInstrumentation.addTransformer(new LineCoverageTransformer(target));
      invoked = true;
    }
  }

  public static boolean wasInvoked() {
    return invoked;
  }
}
