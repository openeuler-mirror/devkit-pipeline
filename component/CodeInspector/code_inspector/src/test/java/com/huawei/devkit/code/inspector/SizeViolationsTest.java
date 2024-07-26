package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * SizeViolationsTest
 *
 * @since 2024-07-22
 */
public class SizeViolationsTest {
    @ParameterizedTest
    @ValueSource(strings = {"FileLength", "LineLength", "MethodLength", "ParameterNumber"})
    void testSizeViolations(String rule) {
        TestUtil.execute("size_violations", rule);
    }
}
