package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class MiscellaneousTest {

    @Test
    void testArrayTypeStyle() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/ArrayTypeStyleCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/ArrayTypeStyle.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testArrayTypeStyle.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testAvoidEscapedUnicodeCharacters() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/AvoidEscapedUnicodeCharactersCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/AvoidEscapedUnicodeCharacters.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testAvoidEscapedUnicodeCharacters.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testCommentsIndentation() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/CommentsIndentationCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/CommentsIndentation.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testCommentsIndentation.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testDescendantToken() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/DescendantTokenCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/DescendantToken.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testDescendantToken.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testIndentation() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/IndentationCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/Indentation.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testIndentation.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testNewlineAtEndOfFile() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/NewlineAtEndOfFileCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/NewlineAtEndOfFile.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testNewlineAtEndOfFile.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testNoCodeInFile() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/NoCodeInFileCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/NoCodeInFile.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testNoCodeInFile.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testOuterTypeFilename() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/OuterTypeFilenameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/OuterTypeFilename.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testOuterTypeFilename.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testTodoComment() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/TodoCommentCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/TodoComment.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testTodoComment.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testUpperEll() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/miscellaneous/UpperEllCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/miscellaneous/UpperEll.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testUpperEll.out", "-f", "sarif", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }


}
