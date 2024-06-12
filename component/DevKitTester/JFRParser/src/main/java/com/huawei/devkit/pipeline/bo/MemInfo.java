package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MemInfo {
    @JsonProperty("t")
    private long startTime;
    @JsonProperty("c")
    private Long committedSize;
    @JsonProperty("r")
    private Long reservedSize;
    @JsonProperty("u")
    private Long heapUsed;

    public MemInfo(long startTime, Long committedSize, Long reservedSize, Long heapUsed) {
        this.startTime = startTime;
        this.committedSize = committedSize;
        this.reservedSize = reservedSize;
        this.heapUsed = heapUsed;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCommittedSize() {
        return committedSize;
    }

    public void setCommittedSize(long committedSize) {
        this.committedSize = committedSize;
    }

    public long getReservedSize() {
        return reservedSize;
    }

    public void setReservedSize(long reservedSize) {
        this.reservedSize = reservedSize;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }
}
