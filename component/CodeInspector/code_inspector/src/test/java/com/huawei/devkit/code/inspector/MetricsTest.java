package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MetricsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "BooleanExpressionComplexity", "ClassDataAbstractionCoupling", "ClassFanOutComplexity", "CyclomaticComplexity",
        "JavaNCSS", "NPathComplexity"
    })
    void testMetrics(String rule) {
        TestUtil.execute("metrics", rule);
    }
}
