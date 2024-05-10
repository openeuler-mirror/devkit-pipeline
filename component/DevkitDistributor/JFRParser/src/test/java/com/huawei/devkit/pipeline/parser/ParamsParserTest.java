package com.huawei.devkit.pipeline.parser;

import com.huawei.devkit.pipeline.bo.CommandLineParams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.args4j.CmdLineException;

class ParamsParserTest {

    @Test
    public void testParse() throws CmdLineException {
        String[] args = new String[]{"-j", "/home/zpp/jmeter.csv",
                "-o", "/home/zpp/devkit_jmeter.html",
                "-f", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2",
                "-n", "10.110.0.2:+10,127.0.0.1:-15"};
        try {
            CommandLineParams params = ParamsParser.parse(args);
            params.fillMaps();
            Assertions.assertEquals(params.getOutput(), "/home/zpp/devkit_jmeter.html");
            Assertions.assertEquals(params.getJmeterResult(), "/home/zpp/jmeter.csv");
            Assertions.assertEquals(params.getJfrPaths().get(0), "10.110.0.2:/home/zpp/demo.jfr");
            Assertions.assertEquals(params.getJfrPaths().get(1), "127.0.0.1:/home/zpp/demo2.jfr2");
            Assertions.assertEquals(params.getJfrPathMap().get("10.110.0.2"), "/home/zpp/demo.jfr");
            Assertions.assertEquals(params.getJfrPathMap().get("127.0.0.1"), "/home/zpp/demo2.jfr2");
            Assertions.assertEquals(params.getNodeTimeGaps().get(0), "10.110.0.2:+10");
            Assertions.assertEquals(params.getNodeTimeGaps().get(1), "127.0.0.1:-15");
            Assertions.assertEquals(params.getNodesTimeGapMap().get("10.110.0.2"), "+10");
            Assertions.assertEquals(params.getNodesTimeGapMap().get("127.0.0.1"), "-15");
        } catch (CmdLineException e) {
            throw e;
        }
    }

    @Test
    public void testParseThrowsExceptionDueToMissingJOption() {
        String[] args = new String[]{"-o", "/home/zpp/devkit_jmeter.html",
                "-f", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2",
                "-n", "10.110.0.2:+10,127.0.0.1:-15"};
        Assertions.assertThrows(CmdLineException.class, () -> ParamsParser.parse(args));
    }

    @Test
    public void testParseThrowsExceptionDueToMissingOOption() {
        String[] args = new String[]{"-j", "/home/zpp/jmeter.csv",
                "-f", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2",
                "-n", "10.110.0.2:+10,127.0.0.1:-15"};
        Assertions.assertThrows(CmdLineException.class, () -> ParamsParser.parse(args));
    }

    @Test
    public void testParseThrowsExceptionDueToMissingFOption() {
        String[] args = new String[]{"-j", "/home/zpp/jmeter.csv", "-o", "/home/zpp/devkit_jmeter.html",
                "-n", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2"};
        Assertions.assertThrows(CmdLineException.class, () -> ParamsParser.parse(args));
    }

    @Test
    public void testParseThrowsExceptionDueToMissingNOption() {
        String[] args = new String[]{"-j", "/home/zpp/jmeter.csv", "-o", "/home/zpp/devkit_jmeter.html",
                "-f", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2"};
        Assertions.assertThrows(CmdLineException.class, () -> ParamsParser.parse(args));
    }

    @Test
    public void testParseThrowsExceptionDueToRedundantOptions() {
        String[] args = new String[]{"-j", "/home/zpp/jmeter.csv", "-o", "/home/zpp/devkit_jmeter.html",
                "-f", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2",
                "-n", "10.110.0.2:/home/zpp/demo.jfr,127.0.0.1:/home/zpp/demo2.jfr2", "-t", ""};
        Assertions.assertThrows(CmdLineException.class, () -> ParamsParser.parse(args));
    }
}