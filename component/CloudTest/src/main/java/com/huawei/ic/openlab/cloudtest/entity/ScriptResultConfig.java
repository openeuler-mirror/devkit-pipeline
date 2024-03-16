/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import lombok.Data;

/**
 * ScriptResultConfig
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Data
public class ScriptResultConfig {
    private String applicationNames;
    private String startAppCommands;
    private String stopAppCommands;
    private String osVersion;
}
