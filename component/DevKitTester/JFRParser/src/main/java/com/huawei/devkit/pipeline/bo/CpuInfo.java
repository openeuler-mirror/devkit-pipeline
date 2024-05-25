package com.huawei.devkit.pipeline.bo;


import com.fasterxml.jackson.annotation.JsonProperty;

public class CpuInfo {
    @JsonProperty("t")
    private long startTime;
    @JsonProperty("s")
    private float jvmSystem;
    @JsonProperty("u")
    private float jvmUser;
    @JsonProperty("m")
    private float machineTotal;

    public CpuInfo(long startTime, float jvmSystem, float jvmUser, float machineTotal) {
        this.startTime = startTime;
        this.jvmSystem = jvmSystem;
        this.jvmUser = jvmUser;
        this.machineTotal = machineTotal;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public float getJvmSystem() {
        return jvmSystem;
    }

    public void setJvmSystem(float jvmSystem) {
        this.jvmSystem = jvmSystem;
    }

    public float getJvmUser() {
        return jvmUser;
    }

    public void setJvmUser(float jvmUser) {
        this.jvmUser = jvmUser;
    }

    public float getMachineTotal() {
        return machineTotal;
    }

    public void setMachineTotal(float machineTotal) {
        this.machineTotal = machineTotal;
    }
}
