package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class JmeterResultParserTest {

    @Test
    public void testParse1() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("result.csv");
        assert resource != null;
        Path resultPath = Paths.get(resource.toURI());
        PerformanceTestResult result = new PerformanceTestResult();
        JmeterResultParser.parse(resultPath.toString(), result);
        result.toSimpleObject();
        Assertions.assertEquals(result.getRt().size(), 2);
        Assertions.assertEquals(result.getSummaries().size(), 2);
        Assertions.assertEquals(result.getSummaries().get(0).getSamples(), 1000);
        Assertions.assertEquals(result.getSummaries().get(1).getSamples(), 1000);
        Assertions.assertEquals(result.getSummaries().get(1).getFailSamples(), 806);
        Assertions.assertEquals(result.getSummaries().get(1).getAverageLatency(), 1.993);
    }

    /**
     * @utbot.classUnderTest {@link JmeterResultParser}
     * @utbot.methodUnderTest {@link JmeterResultParser#parse(String, PerformanceTestResult)}
     */
    @Test
    @DisplayName("parse: arg_0 = null, result = PerformanceTestResult() -> throw NullPointerException")
    public void testParseThrowsNullPointerExceptionWhenArg0IsNull() {
        PerformanceTestResult result = new PerformanceTestResult();
        Assertions.assertThrows(NullPointerException.class, () -> JmeterResultParser.parse(null, result));
    }

    /**
     * @utbot.classUnderTest {@link JmeterResultParser}
     * @utbot.methodUnderTest {@link JmeterResultParser#parse(String, PerformanceTestResult)}
     */
    @Test
    @DisplayName("parse: arg_0 = empty string, result = PerformanceTestResult() -> throw FileNotFoundException")
    public void testParseThrowsFileNotFoundExceptionWhenArg0IsEmpty() {
        PerformanceTestResult result = new PerformanceTestResult();
        Assertions.assertThrows(FileNotFoundException.class, () -> JmeterResultParser.parse("", result));
    }

    @Test
    @DisplayName("parse:  -> ThrowInvalidPathException")
    public void testParseThrowInvalidPathException() {
        PerformanceTestResult result = new PerformanceTestResult();
        String resultPath = "\u0000";
        Assertions.assertThrows(InvalidPathException.class, () -> JmeterResultParser.parse(resultPath, result));
    }


    @Test
    @DisplayName("parse: NullPointerException")
    public void testParseThrowsNullPointerExceptionWhenArg1IsNull() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("result.csv");
        assert resource != null;
        Path resultPath = Paths.get(resource.toURI());
        assertThrows(NullPointerException.class, () -> JmeterResultParser.parse(resultPath.toString(), null));
    }


    /**
     * @utbot.classUnderTest {@link JmeterResultParser}
     * @utbot.methodUnderTest {@link JmeterResultParser#parse(String, PerformanceTestResult)}
     * @utbot.invokes {@link com.huawei.devkit.pipeline.utils.JmeterResultTransfer#transfer(String)}
     * @utbot.throwsException {@link NoClassDefFoundError} in: List<JmeterResult> results = JmeterResultTransfer.transfer(resultPath);
     */
    @Test
    @DisplayName("parse: NullPointerException")
    public void testParseThrowsNullPointerExceptionWhenArg0AndArg1IsNull() {
        assertThrows(NullPointerException.class, () -> JmeterResultParser.parse(null, null));
    }


    @Test
    @DisplayName("parse: arg_0 = '', result = null -> throw FileNotFoundException")
    public void testParseThrowsFNFEWhenArg0IsEmptyString() {
        assertThrows(FileNotFoundException.class, () -> JmeterResultParser.parse("", null));
    }

    @Test
    @DisplayName("parse: arg_0 = '', result = null -> throw InvalidPathException")
    public void testParseThrowsInvalidPathExceptionWhenArg0IsIncorrectString() {
        String resultPath = "\u0000";
        assertThrows(InvalidPathException.class, () -> JmeterResultParser.parse(resultPath, null));
    }

}
