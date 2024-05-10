package com.huawei.devkit.pipeline.bo;

public class LatencyTopInfo {
    private long startTime;
    private long endTime;
    private long key;

    private FlameItem flame;

    public LatencyTopInfo(long startTime, long endTime, long key) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.key = key;
        this.flame = new FlameItem("all", 0);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public FlameItem getFlame() {
        return flame;
    }

    public void setFlame(FlameItem flame) {
        this.flame = flame;
    }

}
