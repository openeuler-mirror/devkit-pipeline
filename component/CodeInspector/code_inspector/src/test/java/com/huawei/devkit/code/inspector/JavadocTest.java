/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JavadocTest
 *
 * @since 2024-07-16
 */
public class JavadocTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "AtclauseOrder", "InvalidJavadocPosition", "JavadocBlockTagLocation", "JavadocContentLocation",
        "JavadocMethod", "JavadocMissingLeadingAsterisk", "JavadocMissingWhitespaceAfterAsterisk",
        "JavadocParagraph", "JavadocTagContinuationIndentation", "JavadocVariable", "MissingJavadocMethod",
        "MissingJavadocType", "NonEmptyAtclauseDescription", "RequireEmptyLineBeforeBlockTagGroup",
        "SingleLineJavadoc", "WriteTag"
    })
    void testMetrics(String rule) {
        TestUtil.execute("javadoc", rule);
    }
}
