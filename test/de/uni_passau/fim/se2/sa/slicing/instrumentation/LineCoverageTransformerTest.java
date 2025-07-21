package de.uni_passau.fim.se2.sa.slicing.instrumentation;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

class LineCoverageTransformerTest {

    @Test
    void testTransform_ShouldReturnOriginalBytes_WhenClassIsIgnored() {
        String target = "com/example/app";
        LineCoverageTransformer transformer = new LineCoverageTransformer(target);

        byte[] originalBytes = new byte[]{0x01, 0x02};

        byte[] result = transformer.transform(
                getClass().getClassLoader(),
                "com/example/other/ClassTest",  // ends with "Test" → ignored
                null,
                null,
                originalBytes
        );

        assertSame(originalBytes, result, "Expected transformer to return original bytes when class is ignored");
    }

    @Test
    void testTransform_ShouldInstrumentClass_WhenClassIsTargeted() {
        String target = "com/example";
        LineCoverageTransformer transformer = new LineCoverageTransformer(target);

        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "com/example/MyClass", null, "java/lang/Object", null);
        cw.visitEnd();

        byte[] classBytes = cw.toByteArray();

        byte[] transformedBytes = transformer.transform(
                getClass().getClassLoader(),
                "com/example/MyClass",
                null,
                null,
                classBytes
        );

        assertNotNull(transformedBytes, "Transformed bytecode should not be null");
        assertNotEquals(classBytes, transformedBytes, "Transformed bytecode should differ from original if instrumented");
    }

    @Test
    void testTransform_ShouldReturnOriginalBytes_OnException() {
        String target = "com/example";
        LineCoverageTransformer transformer = new LineCoverageTransformer(target);

        // Invalid class bytecode that will trigger ClassReader exception
        byte[] invalidBytes = new byte[]{0x00, 0x00};

        byte[] result = transformer.transform(
                getClass().getClassLoader(),
                "com/example/BrokenClass",
                null,
                null,
                invalidBytes
        );

        assertSame(invalidBytes, result, "Expected transformer to fallback to original bytes on error");
    }
}
