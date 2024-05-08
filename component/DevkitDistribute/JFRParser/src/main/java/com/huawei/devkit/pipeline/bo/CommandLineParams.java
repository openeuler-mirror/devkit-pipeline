package com.huawei.devkit.pipeline.bo;

import com.huawei.devkit.pipeline.strategy.MultiHandlerFactory;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLineParams {

    @Option(name = "-j", usage = "the jmeter result path", metaVar = "jmeter_result_path", required = true)
    private String jmeterResult;

    @Option(name = "-o", usage = "the output path", metaVar = "output", required = true)
    private String output;

    @Option(name = "-n", usage = "the jfr path", metaVar = "display", required = true,
            handler = MultiHandlerFactory.MultiFieldOptionHandler.class)
    private List<String> nodeTimeGaps;

    private Map<String, String> nodesTimeGapMap;

    @Option(name = "-f", usage = "the jfr path", metaVar = "display", required = true,
            handler = MultiHandlerFactory.MultiFieldOptionHandler.class)
    private List<String> jfrPaths;

    private Map<String, String> jfrPathMap;

    public CommandLineParams() {
        this.jmeterResult = "";
        this.output = "";
        this.nodeTimeGaps = new ArrayList<>();
        this.nodesTimeGapMap = new HashMap<>();
        this.jfrPathMap = new HashMap<>();
        this.jfrPaths = new ArrayList<>();
    }

    public void fillMaps() {
        for (String argument : jfrPaths) {
            String[] values = argument.split(":");
            jfrPathMap.put(values[0], values[1]);
        }

        for (String argument : nodeTimeGaps) {
            String[] values = argument.split(":");
            nodesTimeGapMap.put(values[0], values[1]);
        }
    }

    public String getJmeterResult() {
        return jmeterResult;
    }

    public void setJmeterResult(String jmeterResult) {
        this.jmeterResult = jmeterResult;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<String> getNodeTimeGaps() {
        return nodeTimeGaps;
    }

    public void setNodeTimeGaps(List<String> nodeTimeGaps) {
        this.nodeTimeGaps = nodeTimeGaps;
    }

    public Map<String, String> getNodesTimeGapMap() {
        return nodesTimeGapMap;
    }

    public void setNodesTimeGapMap(Map<String, String> nodesTimeGapMap) {
        this.nodesTimeGapMap = nodesTimeGapMap;
    }

    public List<String> getJfrPaths() {
        return jfrPaths;
    }

    public void setJfrPaths(List<String> jfrPaths) {
        this.jfrPaths = jfrPaths;
    }

    public Map<String, String> getJfrPathMap() {
        return jfrPathMap;
    }

    public void setJfrPathMap(Map<String, String> jfrPathMap) {
        this.jfrPathMap = jfrPathMap;
    }
}

