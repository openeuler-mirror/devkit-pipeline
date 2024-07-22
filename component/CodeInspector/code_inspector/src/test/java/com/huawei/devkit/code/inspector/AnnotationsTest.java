package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * AnnotationsTest
 *
 * @since 2024-07-18
 */
public class AnnotationsTest {
    @Test
    void testAnnotationLocation() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/annotations/AnnotationLocationCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/annotations/AnnotationLocation.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testAnnotationLocation.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testSuppressWarnings() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/annotations/SuppressWarningsCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/annotations/SuppressWarnings.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testAnnotationLocation.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }
}
