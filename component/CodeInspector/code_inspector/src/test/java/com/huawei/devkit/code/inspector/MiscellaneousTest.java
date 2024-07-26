package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MiscellaneousTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "ArrayTypeStyle", "AvoidEscapedUnicodeCharacters", "CommentsIndentation", "DescendantToken",
        "Indentation", "NoCodeInFile", "OuterTypeFilename", "TodoComment", "UpperEll"
    })
    void testMiscellaneous(String rule) {
        TestUtil.execute("miscellaneous", rule);
    }

}
