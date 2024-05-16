package com.huawei.devkit.pipeline.bo;

public class JmeterReportSummary {

    private String label;

    private long samples;

    private long failSamples;

    private double averageLatency;

    private long minLatency;

    private long maxLatency;

    private long median;

    private long latency99;

    private long latency95;

    private long latency90;

    private long throughput;

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

    public long getMinLatency() {
        return minLatency;
    }

    public void setMinLatency(long minLatency) {
        this.minLatency = minLatency;
    }

    public long getMaxLatency() {
        return maxLatency;
    }

    public void setMaxLatency(long maxLatency) {
        this.maxLatency = maxLatency;
    }

    public long getMedian() {
        return median;
    }

    public void setMedian(long median) {
        this.median = median;
    }

    public long getLatency99() {
        return latency99;
    }

    public void setLatency99(long latency99) {
        this.latency99 = latency99;
    }

    public long getLatency95() {
        return latency95;
    }

    public void setLatency95(long latency95) {
        this.latency95 = latency95;
    }

    public long getLatency90() {
        return latency90;
    }

    public void setLatency90(long latency90) {
        this.latency90 = latency90;
    }

    public long getThroughput() {
        return throughput;
    }

    public void setThroughput(long throughput) {
        this.throughput = throughput;
    }
}
