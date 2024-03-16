/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SystemParams
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Component
@Data
public class SystemParams {
    @Value("${scriptConfigCn}")
    private String scriptConfigCn;

    @Value("${scriptConfigEn}")
    private String scriptConfigEn;

    @Value("${tempDir}")
    private String tempDir;

    @Value("${deployIp}")
    private String deployIP;

    @Value("${uploadFileDir}")
    private String uploadFileDir;

    @Value("${appId}")
    private String appId;

    @Value("${appKey}")
    private String appKey;

    @Value("${mqsTopic}")
    private String mqsTopic;

    @Value("${mqsUrl}")
    private String mqsUrl;

    @Value("${clamdIp}")
    private String clamdIp;

    @Value("${clamdPort}")
    private int clamdPort;

    @Value("${performanceTestUrl}")
    private String performanceTestUrl;


    @Value("${appIdUat}")
    private String appIdUat;

    @Value("${appKeyUat}")
    private String appKeyUat;

    @Value("${mqsTopicUat}")
    private String mqsTopicUat;

    @Value("${mqsUrlUat}")
    private String mqsUrlUat;

    @Value("${kunpengServiceUrl}")
    private String kunpengServiceUrl;

    /**
     * get Script Config
     *
     * @param language language
     * @return Config
     */
    public String getScriptConfig(String language) {
        return "CN".equals(language) ? scriptConfigCn : scriptConfigEn;
    }
}
