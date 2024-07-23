package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * AnnotationsTest
 *
 * @since 2024-07-18
 */
public class AnnotationsTest {
    @ParameterizedTest
    @ValueSource(strings = {"AnnotationLocation", "SuppressWarnings"})
    void testAnnotations(String rule) {
        TestUtil.execute("annotations", rule);
    }
}
