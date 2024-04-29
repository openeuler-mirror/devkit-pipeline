package com.huawei.devkit.pipeline.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JfrMethodSignatureParserTest {

    @Test
    public void testConvertMethodSignatureToNormalString() {
        String methodSignature = "(I)Ljava/lang/String;";
        String method = "java.lang.String.replace";
        String normalString = JfrMethodSignatureParser.convertMethodSignature(methodSignature, method);
        Assertions.assertEquals(normalString, "java.lang.String java.lang.String.replace(int)");
    }

}