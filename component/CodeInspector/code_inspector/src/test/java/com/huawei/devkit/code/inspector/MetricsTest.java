package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class MetricsTest {
    @Test
    void testBooleanExpressionComplexity() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/metrics/BooleanExpressionComplexityCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/metrics/BooleanExpressionComplexity.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testBooleanExpressionComplexity.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testClassDataAbstractionCouplingCase() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/metrics/ClassDataAbstractionCouplingCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/metrics/ClassDataAbstractionCoupling.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testClassDataAbstractionCoupling.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }


    @Test
    void testClassFanOutComplexityCase() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/metrics/ClassFanOutComplexityCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/metrics/ClassFanOutComplexity.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testClassFanOutComplexity.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testCyclomaticComplexityCase() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/metrics/CyclomaticComplexityCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/metrics/CyclomaticComplexity.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testCyclomaticComplexity.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavaNCSSCase() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/metrics/JavaNCSSCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/metrics/JavaNCSS.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavaNCSSCase.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testNPathComplexity() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/metrics/NPathComplexityCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/metrics/NPathComplexity.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testNPathComplexity.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }
}
