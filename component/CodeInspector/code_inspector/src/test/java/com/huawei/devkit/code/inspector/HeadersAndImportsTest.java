package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class HeadersAndImportsTest {

    @Test
    void testRegexpHeader() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/headers_and_imports/RegexpHeaderCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/headers_and_imports/RegexpHeader.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testRegexpHeader.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testAvoidStarImport() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/headers_and_imports/AvoidStarImportCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/headers_and_imports/AvoidStarImport.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testAvoidStarImport.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testCustomImportOrderRules() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/headers_and_imports/CustomImportOrderCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/headers_and_imports/CustomImportOrder.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testCustomImportOrderRules.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testUnusedImports() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/headers_and_imports/UnusedImportsCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/headers_and_imports/UnusedImports.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testUnusedImports.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testRedundantImport() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/headers_and_imports/RedundantImportCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/headers_and_imports/RedundantImport.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testRedundantImport.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }
}
