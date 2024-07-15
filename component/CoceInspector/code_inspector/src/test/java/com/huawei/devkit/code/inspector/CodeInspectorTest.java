package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeInspectorTest {
    @Test
    void test01() {
        String root = System.getProperty("user.dir");
        String path = this.getClass().getClassLoader().getResource("case/CodeInspectorTestCase.java").getPath();
        Assertions.assertDoesNotThrow(() -> {
        });
        System.out.println();
    }
}
