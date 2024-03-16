/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 网站请求蓝区兼容性测试实体类
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Data
public class LabTestReq {
    private String projectId;
    private String userId;

    private String serverIp;
    private int serverPort;
    private String serverUser;
    private String serverPassword;

    @JsonProperty(value = "compatibilityTest")
    private boolean isCompatibilityTest;
    @JsonProperty(value = "reliabilityTest")
    private boolean isReliabilityTest;
    @JsonProperty(value = "securityTest")
    private boolean isSecurityTest;
    @JsonProperty(value = "functionTest")
    private boolean isFunctionTest;
    @JsonProperty(value = "performance")
    private boolean isPerformanceTest;

    private String deployDir;
    private String applicationNames;
    private String startAppCommands;
    private String stopAppCommands;

    private int webPort;

    @JsonProperty(value = "language", defaultValue = "CN")
    private String taskLanguage;
}
