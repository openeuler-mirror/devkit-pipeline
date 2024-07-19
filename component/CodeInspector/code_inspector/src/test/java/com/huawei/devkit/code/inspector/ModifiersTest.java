package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class ModifiersTest {
    @Test
    void testModifierOrder() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/modifiers/ModifierOrderCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/modifiers/ModifierOrder.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testModifierOrder.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testRedundantModifier() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/modifiers/RedundantModifierCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/modifiers/RedundantModifier.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testRedundantModifier.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }
}
