package com.huawei.devkit.pipeline.bo;

public class LatencyTopInfo {
    private long startTime;
    private long endTime;
    private long key;

    public FlameItem getFlame() {
        return flame;
    }

    public void setFlame(FlameItem flame) {
        this.flame = flame;
    }

    private FlameItem flame;

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
}
