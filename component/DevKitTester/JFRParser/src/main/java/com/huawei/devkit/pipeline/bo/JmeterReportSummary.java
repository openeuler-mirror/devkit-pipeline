package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huawei.devkit.pipeline.strategy.DoubleSerialize;

public class JmeterReportSummary {

    private String label;

    private long samples;

    private long failSamples;

    @JsonSerialize(using = DoubleSerialize.class)
    private double averageLatency;

    @JsonSerialize(using = DoubleSerialize.class)
    private double minLatency;

    @JsonSerialize(using = DoubleSerialize.class)
    private double maxLatency;

    @JsonSerialize(using = DoubleSerialize.class)
    private double median;

    @JsonSerialize(using = DoubleSerialize.class)
    private double latency99;

    @JsonSerialize(using = DoubleSerialize.class)
    private double latency95;

    @JsonSerialize(using = DoubleSerialize.class)
    private double latency90;

    @JsonSerialize(using = DoubleSerialize.class)
    private double throughput;

    public JmeterReportSummary(String label) {
        this.label = label;
    }

    public void samplesIncrease() {
        this.samples++;
    }

    public void failSamplesIncrease() {
        this.failSamples++;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getSamples() {
        return samples;
    }

    public void setSamples(long samples) {
        this.samples = samples;
    }

    public long getFailSamples() {
        return failSamples;
    }

    public void setFailSamples(long failSamples) {
        this.failSamples = failSamples;
    }

    public double getAverageLatency() {
        return averageLatency;
    }

    public void setAverageLatency(double averageLatency) {
        this.averageLatency = averageLatency;
    }

    public double getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(double minLatency) {
        this.minLatency = minLatency;
    }

    public double getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(double maxLatency) {
        this.maxLatency = maxLatency;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getLatency99() {
        return latency99;
    }

    public void setLatency99(double latency99) {
        this.latency99 = latency99;
    }

    public double getLatency95() {
        return latency95;
    }

    public void setLatency95(double latency95) {
        this.latency95 = latency95;
    }

    public double getLatency90() {
        return latency90;
    }

    public void setLatency90(double latency90) {
        this.latency90 = latency90;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }
}
