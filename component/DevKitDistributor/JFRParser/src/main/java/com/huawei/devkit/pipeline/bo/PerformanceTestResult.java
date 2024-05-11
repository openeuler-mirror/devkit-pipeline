package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huawei.devkit.pipeline.constants.JFRConstants;
import com.huawei.devkit.pipeline.parser.JFRParser;
import com.huawei.devkit.pipeline.utils.SimplifyResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerformanceTestResult {

    private List<JmeterReportSummary> summaries;

    private List<Long> startTime;

    @JsonIgnore
    private Map<String, List<JmeterRT>> rtMap;

    private Map<String, Map<String, List<Object>>> rt;

    @JsonIgnore
    private Map<String, List<JmeterRT>> frtMap;

    private Map<String, Map<String, List<Object>>> frt;

    @JsonIgnore
    private Map<String, List<JmeterTPS>> tpsMap;

    private Map<String, Map<String, List<Object>>> tps;

    @JsonIgnore
    private Map<String, List<MemInfo>> memoryMap;

    private Map<String, Map<String, List<Object>>> memory;

    @JsonIgnore
    private Map<String, List<CpuInfo>> cpuMap;

    private Map<String, Map<String, List<Object>>> cpu;

    private Map<Long, FlameItem> flame;

    public PerformanceTestResult() {
        this.summaries = new ArrayList<>();
        this.startTime = new ArrayList<>();
        this.rtMap = new HashMap<>();
        this.rt = new HashMap<>();
        this.frtMap = new HashMap<>();
        this.frt = new HashMap<>();
        this.tpsMap = new HashMap<>();
        this.tps = new HashMap<>();
        this.memoryMap = new HashMap<>();
        this.memory = new HashMap<>();
        this.cpuMap = new HashMap<>();
        this.cpu = new HashMap<>();
        this.flame = new HashMap<>();
        // 初始化火焰图root
        this.flame.put(JFRParser.ALL, new FlameItem("all", 0));
    }

    /**
     * 简化返回的响应结果，进一部节省字节
     */
    public void toSimpleObject() {
        List<JmeterRT> totalRTS = this.rtMap.get(JFRConstants.TOTAL_LABEL);
        if (totalRTS != null) {
            this.startTime = totalRTS.stream().map(JmeterRT::getStartTime).collect(Collectors.toList());
            this.toSimpleObject(this.rtMap, this.rt, JmeterRT.class);
            this.toSimpleObject(this.frtMap, this.frt, JmeterRT.class);
            this.toSimpleObject(this.tpsMap, this.tps, JmeterTPS.class);
        }
        this.toSimpleObject(this.memoryMap, this.memory, MemInfo.class);
        this.toSimpleObject(this.cpuMap, this.cpu, CpuInfo.class);
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

    public Map<String, List<JmeterRT>> getRtMap() {
        return rtMap;
    }

    public void setRtMap(Map<String, List<JmeterRT>> rtMap) {
        this.rtMap = rtMap;
    }

    public Map<String, List<JmeterRT>> getFrtMap() {
        return frtMap;
    }

    public void setFrtMap(Map<String, List<JmeterRT>> frtMap) {
        this.frtMap = frtMap;
    }

    public Map<String, List<JmeterTPS>> getTpsMap() {
        return tpsMap;
    }

    public void setTpsMap(Map<String, List<JmeterTPS>> tpsMap) {
        this.tpsMap = tpsMap;
    }

    public Map<Long, FlameItem> getFlame() {
        return flame;
    }

    public void setFlame(Map<Long, FlameItem> flame) {
        this.flame = flame;
    }

    public Map<String, List<MemInfo>> getMemoryMap() {
        return memoryMap;
    }

    public void setMemoryMap(Map<String, List<MemInfo>> memoryMap) {
        this.memoryMap = memoryMap;
    }

    public Map<String, List<CpuInfo>> getCpuMap() {
        return cpuMap;
    }

    public void setCpuMap(Map<String, List<CpuInfo>> cpuMap) {
        this.cpuMap = cpuMap;
    }

    public Map<String, Map<String, List<Object>>> getRt() {
        return rt;
    }

    public void setRt(Map<String, Map<String, List<Object>>> rt) {
        this.rt = rt;
    }

    public Map<String, Map<String, List<Object>>> getFrt() {
        return frt;
    }

    public void setFrt(Map<String, Map<String, List<Object>>> frt) {
        this.frt = frt;
    }

    public Map<String, Map<String, List<Object>>> getTps() {
        return tps;
    }

    public void setTps(Map<String, Map<String, List<Object>>> tps) {
        this.tps = tps;
    }

    public Map<String, Map<String, List<Object>>> getMemory() {
        return memory;
    }

    public void setMemory(Map<String, Map<String, List<Object>>> memory) {
        this.memory = memory;
    }

    public Map<String, Map<String, List<Object>>> getCpu() {
        return cpu;
    }

    public void setCpu(Map<String, Map<String, List<Object>>> cpu) {
        this.cpu = cpu;
    }

    public List<Long> getStartTime() {
        return startTime;
    }

    public void setStartTime(List<Long> startTime) {
        this.startTime = startTime;
    }

    private <T> void toSimpleObject(Map<String, List<T>> origin, Map<String, Map<String, List<Object>>> target, Class<T> clazz) {
        for (Map.Entry<String, List<T>> entry : origin.entrySet()) {
            target.put(entry.getKey(), SimplifyResponse.simplify(entry.getValue(), clazz));
        }
    }
}
