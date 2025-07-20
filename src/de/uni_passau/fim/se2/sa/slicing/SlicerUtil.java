package de.uni_passau.fim.se2.sa.slicing;

import de.uni_passau.fim.se2.sa.slicing.graph.ProgramDependenceGraph;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.lang.reflect.Method;

public class SlicerUtil {

    /**
     * Executes the provided test case on the given class via the JUnit test framework.
     *
     * @param className The name of the class to be tested.
     * @param testCase  The name of the test case to be executed.
     */
    public static void executeTest(String className, String testCase) {
        try {
            String testClassName = className + "Test";
            Class<?> testClass = Class.forName(testClassName);
            Method method = testClass.getDeclaredMethod(testCase);

            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectMethod(testClass, method))
                    .build();

            Launcher launcher = LauncherFactory.create();

            launcher.execute(request);


        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException("Test class or method not found", e);
        }
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
