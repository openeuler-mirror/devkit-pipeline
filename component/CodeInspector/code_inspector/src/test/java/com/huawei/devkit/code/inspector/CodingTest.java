package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CodingTest
 *
 * @since 2024-07-20
 */
public class CodingTest {
    @ParameterizedTest
    @ValueSource(strings = {"ConstructorsDeclarationGrouping", "CovariantEquals", "DeclarationOrder",
        "DefaultComesLast", "EmptyStatement", "EqualsAvoidNull"
    })
    void testCoding(String rule) {
        TestUtil.execute("coding", rule);
    }
}
