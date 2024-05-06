package com.huawei.devkit.pipeline.bo;

import com.huawei.devkit.pipeline.parse.JFRParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTestResult {

    private String rt;

    private String frt;

    private String tps;

    private String qps;

    private Map<String, List<MemInfo>> memory;

    private Map<String, List<CpuInfo>> cpu;

    private Map<Long, FlameItem> flame;

    public PerformanceTestResult() {
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
