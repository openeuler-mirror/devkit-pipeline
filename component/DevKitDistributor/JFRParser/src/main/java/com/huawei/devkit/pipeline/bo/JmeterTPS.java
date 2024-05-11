package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JmeterTPS {
    @JsonProperty("t")
    private long startTime;
    @JsonProperty("s")
    private int tps;

    public JmeterTPS(long startTime, int tps) {
        this.startTime = startTime;
        this.tps = tps;
    }
}
