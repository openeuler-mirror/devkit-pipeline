package com.huawei.devkit.pipeline.bo;

public class JmeterResult {
    private long startTime;
    private int responseCode;
    private int latency;
    private String label;

    public JmeterResult(long startTime, int responseCode, int latency, String label) {
        this.startTime = startTime;
        this.responseCode = responseCode;
        this.latency = latency;
        this.label = label;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
