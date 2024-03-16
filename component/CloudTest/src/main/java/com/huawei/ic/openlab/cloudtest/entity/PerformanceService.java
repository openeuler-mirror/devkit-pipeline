/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import lombok.Data;

/**
 * PerformanceService
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Data
public class PerformanceService {
    private String id;
    private String deviceId;
    private String serviceIp;
    private String loginAccount;
    private String loginPassword;
    private String status;
    private int taskNum;
}
