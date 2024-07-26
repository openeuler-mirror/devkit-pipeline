package com.huawei.devkit.code.inspector;

import com.huawei.devkit.code.inspector.utils.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CodingTest
 *
 * @since 2024-07-20
 */
@Slf4j
public class CodingTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "ConstructorsDeclarationGrouping", "CovariantEquals", "DeclarationOrder", "DefaultComesLast",
        "EmptyStatement", "EqualsAvoidNull", "EqualsHashCode", "FallThrough", "HiddenField",
        "IllegalCatch", "IllegalInstantiation", "IllegalThrows", "InnerAssignment", "MagicNumber",
        "MissingSwitchDefault", "ModifiedControlVariable", "MultipleStringLiterals", "MultipleVariableDeclarations",
        "NestedForDepth", "NestedIfDepth", "NestedTryDepth", "NoClone", "NoFinalizer",
        "OneStatementPerLine", "OverloadMethodsDeclarationOrder", "ParameterAssignment", "SimplifyBooleanExpression",
        "SimplifyBooleanReturn", "StringLiteralEquality", "SuperClone", "UnnecessarySemicolonAfterOuterTypeDeclaration",
        "UnnecessarySemicolonAfterTypeMemberDeclaration", "UnnecessarySemicolonInTryWithResources",
        "UnusedLocalVariable", "VariableDeclarationUsageDistance",
    })
    void testCoding(String rule) {
        log.info(rule);
        TestUtil.execute("coding", rule);
    }
}
