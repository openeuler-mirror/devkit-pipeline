/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * JavadocTest
 *
 * @since 2024-07-16
 */
public class JavadocTest {
    @Test
    void testAtclauseOrder() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/AtclauseOrderCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/AtclauseOrder.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testAtclauseOrder.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testInvalidJavadocPosition() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/InvalidJavadocPositionCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/InvalidJavadocPosition.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testInvalidJavadocPosition.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocBlockTagLocation() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocBlockTagLocationCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocBlockTagLocation.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocBlockTagLocation.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocContentLocation() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocContentLocationCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocContentLocation.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocContentLocation.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocMethod() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocMethodCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocMethod.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocMethod.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocMissingLeadingAsterisk() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocMissingLeadingAsteriskCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocMissingLeadingAsterisk.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocMissingLeadingAsterisk.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocMissingWhitespaceAfterAsterisk() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocMissingWhitespaceAfterAsteriskCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocMissingWhitespaceAfterAsterisk.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocMissingWhitespaceAfterAsterisk.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocParagraph() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocParagraphCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocParagraph.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocParagraph.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }


    @Test
    void testJavadocTagContinuationIndentation() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocTagContinuationIndentationCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocTagContinuationIndentation.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocTagContinuationIndentation.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testJavadocVariable() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/JavadocVariableCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/JavadocVariable.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testJavadocVariable.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testMissingJavadocMethod() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/MissingJavadocMethodCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/MissingJavadocMethod.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testMissingJavadocMethod.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }


    @Test
    void testMissingJavadocType() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/MissingJavadocTypeCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/MissingJavadocType.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testMissingJavadocType.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testNonEmptyAtclauseDescription() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/NonEmptyAtclauseDescriptionCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/NonEmptyAtclauseDescription.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testNonEmptyAtclauseDescription.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testRequireEmptyLineBeforeBlockTagGroup() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/RequireEmptyLineBeforeBlockTagGroupCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/RequireEmptyLineBeforeBlockTagGroup.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testRequireEmptyLineBeforeBlockTagGroup.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testSingleLineJavadoc() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/SingleLineJavadocCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/SingleLineJavadoc.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testSingleLineJavadoc.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testWriteTag() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/javadoc/WriteTagCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/javadoc/WriteTag.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testWriteTag.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

}
