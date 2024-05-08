package com.huawei.devkit.pipeline.bo;

import com.huawei.devkit.pipeline.parser.JFRParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTestResult {

    private List<JmeterReportSummary> summaries;

    private Map<String, List<JmeterRT>> rt;

    private Map<String, List<JmeterRT>> frt;

    private Map<String, List<JmeterTPS>> tps;

    private Map<String, List<MemInfo>> memory;

    private Map<String, List<CpuInfo>> cpu;

    private Map<Long, FlameItem> flame;

    public PerformanceTestResult() {
        this.summaries = new ArrayList<>();
        this.rt = new HashMap<>();
        this.frt = new HashMap<>();
        this.tps = new HashMap<>();
        this.memory = new HashMap<>();
        this.cpu = new HashMap<>();
        this.flame = new HashMap<>();
        // 初始化火焰图root
        this.flame.put(JFRParser.ALL, new FlameItem("all", 0));
    }

    public void toStandardFlames() {
        for (FlameItem item : this.flame.values()) {
            item.toStandardFlame();
        }
    }

    public List<JmeterReportSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<JmeterReportSummary> summaries) {
        this.summaries = summaries;
    }

    public Map<String, List<JmeterRT>> getRt() {
        return rt;
    }

    public void setRt(Map<String, List<JmeterRT>> rt) {
        this.rt = rt;
    }

    public Map<String, List<JmeterRT>> getFrt() {
        return frt;
    }

    public void setFrt(Map<String, List<JmeterRT>> frt) {
        this.frt = frt;
    }

    public Map<String, List<JmeterTPS>> getTps() {
        return tps;
    }

    public void setTps(Map<String, List<JmeterTPS>> tps) {
        this.tps = tps;
    }

    public Map<Long, FlameItem> getFlame() {
        return flame;
    }

    public void setFlame(Map<Long, FlameItem> flame) {
        this.flame = flame;
    }


    public Map<String, List<MemInfo>> getMemory() {
        return memory;
    }

    public void setMemory(Map<String, List<MemInfo>> memory) {
        this.memory = memory;
    }

    public Map<String, List<CpuInfo>> getCpu() {
        return cpu;
    }

    public void setCpu(Map<String, List<CpuInfo>> cpu) {
        this.cpu = cpu;
    }

}
