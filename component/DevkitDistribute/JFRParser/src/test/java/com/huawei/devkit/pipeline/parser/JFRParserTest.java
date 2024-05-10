package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class JFRParserTest {
    ///region Test suites for executable com.huawei.devkit.pipeline.parser.JFRParser.parse
    @Test
    @DisplayName("parse: arg_0 = Non-existent files")
    public void testParseThrowException() {
        assertThrows(Exception.class, () -> JFRParser.parse("/tmp/null", null, -255, null));
    }

    @Test
    @DisplayName("parse: arg_1 = null")
    public void testParseThrowNullPointerException1() {
        URL resource = this.getClass().getClassLoader().getResource("avrora.jfr");
        String path = resource.getPath();
        PerformanceTestResult result = new PerformanceTestResult();
        assertThrows(NullPointerException.class, () -> JFRParser.parse(path, null, -6134991, result));
    }

    @Test
    @DisplayName("parse: arg_3 = null")
    public void testParseThrowNullPointerException2() throws IOException {
        URL resource = this.getClass().getClassLoader().getResource("avrora.jfr");
        String path = resource.getPath();
        List<LatencyTopInfo> latencyTopInfos = new ArrayList<>();
        latencyTopInfos.add(new LatencyTopInfo(1713159115349L, 1713159116349L, 1713159115349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159116349L, 1713159117349L, 1713159116349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159117349L, 1713159118349L, 1713159117349L));
        assertThrows(NullPointerException.class, () -> JFRParser.parse(path, latencyTopInfos, -6134991, null));
    }


    @Test
    @DisplayName("parse: arg_1 = Incorrectly formatted files")
    public void testParseThrowIOException() {
        URL resource = this.getClass().getClassLoader().getResource("error.jfr");
        String path = resource.getPath();
        List<LatencyTopInfo> latencyTopInfos = new ArrayList<>();
        latencyTopInfos.add(new LatencyTopInfo(1713159115349L, 1713159116349L, 1713159115349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159116349L, 1713159117349L, 1713159116349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159117349L, 1713159118349L, 1713159117349L));
        PerformanceTestResult result = new PerformanceTestResult();
        assertThrows(IOException.class, () -> JFRParser.parse(path, latencyTopInfos, -6134991, result));
    }

    @Test
    @DisplayName("parse: normal parse")
    public void testParseNormal() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("avrora.jfr");
        String path = resource.getPath();
        List<LatencyTopInfo> latencyTopInfos = new ArrayList<>();
        latencyTopInfos.add(new LatencyTopInfo(1713159115349L, 1713159116349L, 1713159115349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159116349L, 1713159117349L, 1713159116349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159117349L, 1713159118349L, 1713159117349L));
        PerformanceTestResult result = new PerformanceTestResult();
        JFRParser.parse(path, latencyTopInfos, -6134991, result);
        result.toStandardFlames();
        Assertions.assertEquals(result.getFlame().size(), 4);
        Assertions.assertEquals(result.getFlame().get(1713159115349L).getValue(), 1);
        Assertions.assertEquals(result.getFlame().get(1713159116349L).getValue(), 23);
        Assertions.assertEquals(result.getFlame().get(1713159117349L).getValue(), 18);
        Assertions.assertEquals(result.getFlame().get(-1L).getValue(), 1463);
        Assertions.assertEquals(result.getCpuMap().get("avrora.jfr").size(), 70);
        Assertions.assertEquals(result.getMemoryMap().get("avrora.jfr").size(), 48);
    }
}
