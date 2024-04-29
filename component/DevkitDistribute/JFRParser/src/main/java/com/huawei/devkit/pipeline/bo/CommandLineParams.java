package com.huawei.devkit.pipeline.bo;

import com.huawei.devkit.pipeline.strategy.MultiHandlerFactory;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

public class CommandLineParams {

    @Option(name = "-j", usage = "the jmeter result path", metaVar = "jmeter_result_path", required = true, help = true)
    private String jmeterResult;

    @Option(name = "-o", usage = "the output path", metaVar = "output")
    private String output;

    @Option(name = "-f", usage = "the jfr path", metaVar = "display",
            handler = MultiHandlerFactory.MultiFieldOptionHandler.class)
    private List<String> jfrPaths;

    @Option(name = "-t", usage = "the top 10 latency",
            metaVar = "top1,top2...topN", handler = MultiHandlerFactory.MultiIntegerOptionHandler.class)
    private List<Integer> top10;

    public CommandLineParams() {
        this.jmeterResult = "";
        this.output = "";
        this.jfrPaths = new ArrayList<>();
        this.top10 = new ArrayList<>();
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

    public List<String> getJfrPaths() {
        return jfrPaths;
    }

    public void setJfrPaths(List<String> jfrPaths) {
        this.jfrPaths = jfrPaths;
    }

    public List<Integer> getTop10() {
        return top10;
    }

    public void setTop10(List<Integer> top10) {
        this.top10 = top10;
    }
}

