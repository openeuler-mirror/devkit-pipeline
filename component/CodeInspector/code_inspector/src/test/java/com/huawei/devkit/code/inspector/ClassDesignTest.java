package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ClassDesignTest
 *
 * @since 2024-07-20
 */
public class ClassDesignTest {
    @ParameterizedTest
    @ValueSource(strings = {"OneTopLevelClass", "ThrowsCount", "VisibilityModifier"})
    void testClassDesign(String rule) {
        TestUtil.execute("class_design", rule);
    }
}
