package de.uni_passau.fim.se2.sa.slicing;

import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;

public class SlicerUtil {

    /**
     * Executes the provided test case on the given class via the JUnit test framework.
     *
     * @param className The name of the class to be tested.
     * @param testCase  The name of the test case to be executed.
     */
    public static void executeTest(String className, String testCase) {
        // TODO Implement execution of test method here
        throw new UnsupportedOperationException("Execution of test method missing");
    }

    /**
     * Simplifies the given program dependence graph by removing all nodes and corresponding edges
     * that were not covered by the executed test.
     *
     * @param pPDG The program dependence graph to simplify.
     * @return The simplified program dependence graph.
     */
    public static ProgramDependenceGraph simplify(final ProgramDependenceGraph pPDG) {
        // TODO Implement simplification of program-dependence graph for dynamic slicing
        throw new UnsupportedOperationException("Simplification of PDG not implemented");
    }
}
