package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * WhitespacesTest
 *
 * @since 2024-07-22
 */
public class WhitespacesTest {
    @ParameterizedTest
    @ValueSource(strings = {"EmptyForInitializerPad", "EmptyForIteratorPad", "EmptyLineSeparator", "FileTabCharacter",
        "GenericWhitespace", "MethodParamPad", "NoLineWrap", "NoWhitespaceAfter", "NoWhitespaceBefore",
        "NoWhitespaceBeforeCaseDefaultColon", "OperatorWrap", "ParenPad", "SeparatorWrap",
        "SingleSpaceSeparator", "TypecastParenPad", "WhitespaceAfter", "WhitespaceAround"
    })
    void testWhitespaces(String rule) {
        TestUtil.execute("whitespaces", rule);
    }
}
