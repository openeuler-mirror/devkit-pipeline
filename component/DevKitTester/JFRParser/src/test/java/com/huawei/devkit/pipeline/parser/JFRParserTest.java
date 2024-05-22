package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import com.huawei.devkit.pipeline.bo.PerformanceTestResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class JFRParserTest {
    ///region Test suites for executable com.huawei.devkit.pipeline.parser.JFRParser.parse
    @Test
    @DisplayName("parse: arg_0 = Non-existent files")
    public void testParseThrowException() {
        assertThrows(Exception.class, () -> JFRParser.parse("/tmp/null", null, -255, null, null));
    }

    @Test
    @DisplayName("parse: arg_1 = null")
    public void testParseThrowNullPointerException1() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("avrora.jfr");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        PerformanceTestResult result = new PerformanceTestResult();
        String nodeIP = "127.0.0.1";
        result.getCpuMap().put(nodeIP, new HashMap<>());
        result.getMemoryMap().put(nodeIP, new HashMap<>());
        assertThrows(NullPointerException.class, () -> JFRParser.parse(path.toString(), null, -6134991, result, nodeIP));
    }

    @Test
    @DisplayName("parse: arg_3 = null")
    public void testParseThrowNullPointerException2() throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("avrora.jfr");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        List<LatencyTopInfo> latencyTopInfos = new ArrayList<>();
        latencyTopInfos.add(new LatencyTopInfo(1713159115349L, 1713159116349L, 1713159115349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159116349L, 1713159117349L, 1713159116349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159117349L, 1713159118349L, 1713159117349L));
        assertThrows(NullPointerException.class, () -> JFRParser.parse(path.toString(), latencyTopInfos, -6134991, null, null));
    }


    @Test
    @DisplayName("parse: arg_1 = Incorrectly formatted files")
    public void testParseThrowIOException() throws URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("error.jfr");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        List<LatencyTopInfo> latencyTopInfos = new ArrayList<>();
        latencyTopInfos.add(new LatencyTopInfo(1713159115349L, 1713159116349L, 1713159115349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159116349L, 1713159117349L, 1713159116349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159117349L, 1713159118349L, 1713159117349L));
        PerformanceTestResult result = new PerformanceTestResult();
        String nodeIP = "127.0.0.1";
        result.getCpuMap().put(nodeIP, new HashMap<>());
        result.getMemoryMap().put(nodeIP, new HashMap<>());
        assertThrows(IOException.class, () -> JFRParser.parse(path.toString(), latencyTopInfos, -6134991, result, nodeIP));
    }

    @Test
    @DisplayName("parse: normal parse")
    public void testParseNormal() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("avrora.jfr");
        assert resource != null;
        Path path = Paths.get(resource.toURI());
        List<LatencyTopInfo> latencyTopInfos = new ArrayList<>();
        latencyTopInfos.add(new LatencyTopInfo(1713159115349L, 1713159116349L, 1713159115349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159116349L, 1713159117349L, 1713159116349L));
        latencyTopInfos.add(new LatencyTopInfo(1713159117349L, 1713159118349L, 1713159117349L));
        PerformanceTestResult result = new PerformanceTestResult();
        String nodeIP = "127.0.0.1";
        result.getCpuMap().put(nodeIP, new HashMap<>());
        result.getMemoryMap().put(nodeIP, new HashMap<>());
        JFRParser.parse(path.toString(), latencyTopInfos, -6134991, result, nodeIP);
        result.toStandardFlames();
        Assertions.assertEquals(result.getFlame().size(), 4);
        Assertions.assertEquals(result.getFlame().get(1713159115349L).getValue(), 1);
        Assertions.assertEquals(result.getFlame().get(1713159116349L).getValue(), 23);
        Assertions.assertEquals(result.getFlame().get(1713159117349L).getValue(), 18);
        Assertions.assertEquals(result.getFlame().get(-1L).getValue(), 1463);
        Assertions.assertEquals(result.getCpuMap().get(nodeIP).get("avrora.jfr").size(), 70);
        Assertions.assertEquals(result.getMemoryMap().get(nodeIP).get("avrora.jfr").size(), 48);
    }
}
