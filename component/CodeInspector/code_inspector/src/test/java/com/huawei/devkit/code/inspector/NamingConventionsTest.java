package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

public class NamingConventionsTest {

    @Test
    void testRedundantModifier() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/AbbreviationAsWordInNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/AbbreviationAsWordInName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testAbbreviationAsWordInName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testClassTypeParameterName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/ClassTypeParameterNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/ClassTypeParameterName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testClassTypeParameterName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testConstantName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/ConstantNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/ConstantName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testConstantName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testIllegalIdentifierName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/IllegalIdentifierNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/IllegalIdentifierName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testIllegalIdentifierName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testInterfaceTypeParameterName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/InterfaceTypeParameterNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/InterfaceTypeParameterName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testInterfaceTypeParameterName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testLocalFinalVariableName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/LocalFinalVariableNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/LocalFinalVariableName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testLocalFinalVariableName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testLocalVariableName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/LocalVariableNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/LocalVariableName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testLocalVariableName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testMemberName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/MemberNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/MemberName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testMemberName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testMethodName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/MethodNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/MethodName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testMethodName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testMethodTypeParameterName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/MethodTypeParameterNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/MethodTypeParameterName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testMethodTypeParameterName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testPackageName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/PackageNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/PackageName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testPackageName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testParameterName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/ParameterNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/ParameterName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testParameterName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testPatternVariableName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/PatternVariableNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/PatternVariableName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testPatternVariableName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testRecordComponentName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/RecordComponentNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/RecordComponentName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testRecordComponentName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testRecordTypeParameterName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/RecordTypeParameterNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/RecordTypeParameterName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testRecordTypeParameterName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testStaticVariableName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/StaticVariableNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/StaticVariableName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testStaticVariableName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

    @Test
    void testTypeName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("case/naming_conventions/TypeNameCase.java")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
                .getResource("single_rules/naming_conventions/TypeName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testTypeName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }


}
