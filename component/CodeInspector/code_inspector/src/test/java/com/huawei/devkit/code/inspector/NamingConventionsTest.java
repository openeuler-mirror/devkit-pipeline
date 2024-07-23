package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;

public class NamingConventionsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "AbbreviationAsWordInName", "ClassTypeParameterName", "ConstantName", "IllegalIdentifierName",
        "InterfaceTypeParameterName", "LambdaParameterName", "LocalFinalVariableName", "LocalVariableName",
        "MemberName", "MethodName", "MethodTypeParameterName", "ParameterName",
        "PatternVariableName", "RecordComponentName", "RecordTypeParameterName", "StaticVariableName",
        "AbbreviationAsWordInName", "ClassTypeParameterName", "ConstantName", "IllegalIdentifierName",
        "TypeName"
    })
    void testNamingConventions(String rule) {
        TestUtil.execute("naming_conventions", rule);
    }
    
    @Test
    void testPackageName() {
        String root = System.getProperty("user.dir");
        String filePath = Objects.requireNonNull(this.getClass().getClassLoader()
            .getResource("case/naming_conventions/package_name")).getPath();
        String configPath = Objects.requireNonNull(this.getClass().getClassLoader()
            .getResource("single_rules/naming_conventions/PackageName.xml")).getPath();
        String[] args = new String[]{"-c", configPath, "-o", root + "/testPackageName.out", "-f", "json", filePath};
        Assertions.assertDoesNotThrow(() -> CodeInspector.main(args));
    }

}
