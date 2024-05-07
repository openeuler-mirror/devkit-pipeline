package com.huawei.devkit.pipeline.bo;

public class JmeterResult {
    private long startTime;
    private int responseCode;
    private int latency;

    public JmeterResult(long startTime, int responseCode, int latency) {
        this.startTime = startTime;
        this.responseCode = responseCode;
        this.latency = latency;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }
}
