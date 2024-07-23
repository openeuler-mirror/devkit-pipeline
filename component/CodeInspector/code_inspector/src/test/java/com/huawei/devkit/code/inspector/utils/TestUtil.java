package com.huawei.devkit.code.inspector.utils;

import com.huawei.devkit.code.inspector.CodeInspector;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;

/**
 * TestUtil
 *
 * @since 2024-07-23
 */
public class TestUtil {
    public static void execute(String module, String rule) {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(TestUtil.class.getClassLoader()
            .getResource("case/" + module + "/" + rule + "Case.java")).getPath();
        String configPath = Objects.requireNonNull(TestUtil.class.getClassLoader()
            .getResource("single_rules/" + module + "/" + rule + ".xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/test" + rule + ".out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }
}
