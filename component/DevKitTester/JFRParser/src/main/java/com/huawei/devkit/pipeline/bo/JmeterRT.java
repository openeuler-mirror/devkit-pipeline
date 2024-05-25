package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JmeterRT {
    @JsonIgnore
    private Long startTime;
    @JsonProperty("s")
    private Double responseTime;

    public JmeterRT(long startTime, Double responseTime) {
        this.startTime = startTime;
        this.responseTime = responseTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }
}
