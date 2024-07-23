package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;

/**
 * AnnotationsTest
 *
 * @since 2024-07-18
 */
public class BlockCheckTest {
    @ParameterizedTest
    @ValueSource(strings = {"LeftCurly", "AvoidNestedBlocks", "EmptyBlock", "NeedBraces"})
    void testBlockChecks(String rule) {
        TestUtil.execute("block_checks", rule);
    }

    @ParameterizedTest
    @CsvSource(value = {"RightCurly, RightCurlyAlone", "RightCurly, RightCurlySame"})
    void testRightCurly(String module, String caseName) {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
            .getResource("case/block_checks/" + caseName + "Case.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
            .getResource("single_rules/block_checks/" + module + ".xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/test" + caseName + ".out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }
}