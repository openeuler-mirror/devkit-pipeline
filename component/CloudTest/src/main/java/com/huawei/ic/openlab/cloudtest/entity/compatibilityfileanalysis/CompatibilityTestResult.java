/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * CompatibilityTestResult
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Data
public class CompatibilityTestResult {
    private String id;
    private String result;
    private List<String> evidence = new ArrayList<>();
    private List<String> evidenceEn = new ArrayList<>();
    private String reason;
    private String reasonEn;
    private String osVersion;
    private List<String> applicationNames;
    private List<String> startAppCommands;
    private List<String> stopAppCommands;
    private List<String> deployDir;

    /**
     * CompatibilityTestResult
     *
     * @param testName test Name
     */
    public CompatibilityTestResult(String testName) {
        this.id = testName;
    }
}
