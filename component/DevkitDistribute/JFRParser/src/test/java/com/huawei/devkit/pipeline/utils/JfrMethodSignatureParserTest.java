package com.huawei.devkit.pipeline.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JfrMethodSignatureParserTest {

    @Test
    public void testConvertMethodSignature01() {
        String methodSignature = "(I)Ljava/lang/String;";
        String method = "java.lang.String.replace";
        String normalString = JfrMethodSignatureParser.convertMethodSignature(methodSignature, method);
        Assertions.assertEquals(normalString, "java.lang.String java.lang.String.replace(int)");
    }

    @Test
    public void testConvertMethodSignature02() {
        String methodSignature = "([Z)Ljava/lang/String;";
        String method = "java.lang.String.replace";
        String normalString = JfrMethodSignatureParser.convertMethodSignature(methodSignature, method);
        Assertions.assertEquals(normalString, "java.lang.String java.lang.String.replace(boolean[])");
    }

    @Test
    public void testConvertMethodSignature03() {
        String methodSignature = "([[I[Ljava/util/List;)Ljava/lang/String;";
        String method = "java.lang.String.replace";
        String normalString = JfrMethodSignatureParser.convertMethodSignature(methodSignature, method);
        Assertions.assertEquals(normalString, "java.lang.String java.lang.String.replace(int[][],java.util.List[])");
    }

    @Test
    public void testConvertMethodSignature04() {
        String methodSignature = "([Ljava/util/List;)V";
        String method = "java.lang.String.replace";
        String normalString = JfrMethodSignatureParser.convertMethodSignature(methodSignature, method);
        Assertions.assertEquals(normalString, "void java.lang.String.replace(java.util.List[])");
    }

    @Test
    public void testConvertMethodSignatureFail01() {
        String methodSignature = "([[I;[Ljava/util/List;)Ljava/lang/String;";
        String method = "java.lang.String.replace";
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> JfrMethodSignatureParser.convertMethodSignature(methodSignature, method));
    }

    @Test
    public void testConvertMethodSignatureFail02() {
        String methodSignature = "([[Ijava/util/List;)Ljava/lang/String;";
        String method = "java.lang.String.replace";
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> JfrMethodSignatureParser.convertMethodSignature(methodSignature, method));
    }
}