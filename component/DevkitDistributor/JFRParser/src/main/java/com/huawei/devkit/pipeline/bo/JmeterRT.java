package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JmeterRT {
    @JsonProperty("t")
    private long startTime;
    @JsonProperty("s")
    private double responseTime;

    public JmeterRT(long startTime, double responseTime) {
        this.startTime = startTime;
        this.responseTime = responseTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }
}
