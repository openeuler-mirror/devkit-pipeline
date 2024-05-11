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

}
