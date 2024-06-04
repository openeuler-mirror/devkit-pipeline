package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JmeterTPS {
    @JsonIgnore
    private Long startTime;
    @JsonProperty("s")
    private int tps;

    public JmeterTPS(long startTime, int tps) {
        this.startTime = startTime;
        this.tps = tps;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public int getTps() {
        return tps;
    }

    public void setTps(int tps) {
        this.tps = tps;
    }
}
