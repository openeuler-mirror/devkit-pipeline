/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.entity;

import lombok.Builder;
import lombok.Data;

/**
 * RuleViolationInfo
 *
 * @since 2024-07-11
 */
@Data
@Builder
public class RuleViolationInfo {
    private int id;
    private String ruleId;
    private int filePathHash;
    private String filePath;
    private String line;
    private int lineno;
    private String message;
    private String mergeId;
    private boolean shielded;
    private boolean commitRequestToShield;
    private long time;
    private int level;
}
