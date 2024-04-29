package com.huawei.devkit.pipeline.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTestResult {

    private String rt;

    private String frt;

    private String tps;

    private String qps;

    private List<MemInfo> memory;

    private List<CpuInfo> cpu;

    private Map<Integer, List<FlameItem>> flame;

    public PerformanceTestResult() {
        this.memory = new ArrayList<>(128);
        this.cpu = new ArrayList<>(128);
        this.flame = new HashMap<>();
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getFrt() {
        return frt;
    }

    public void setFrt(String frt) {
        this.frt = frt;
    }

    public String getTps() {
        return tps;
    }

    public void setTps(String tps) {
        this.tps = tps;
    }

    public String getQps() {
        return qps;
    }

    public void setQps(String qps) {
        this.qps = qps;
    }

    public List<MemInfo> getMemory() {
        return memory;
    }

    public void setMemory(List<MemInfo> memory) {
        this.memory = memory;
    }

    public List<CpuInfo> getCpu() {
        return cpu;
    }

    public void setCpu(List<CpuInfo> cpu) {
        this.cpu = cpu;
    }

    public Map<Integer, List<FlameItem>> getFlame() {
        return flame;
    }

    public void setFlame(Map<Integer, List<FlameItem>> flame) {
        this.flame = flame;
    }
}
