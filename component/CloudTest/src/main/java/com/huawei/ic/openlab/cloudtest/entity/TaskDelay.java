/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import com.huawei.ic.openlab.cloudtest.util.Constants;
import com.huawei.ic.openlab.cloudtest.util.ToolUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TaskDelay
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class TaskDelay {
    private static final long PERFORMANCE_DELAY = 60 * 60 * 1000L;
    private static final long DELAY = 60 * 60 * 1000L;

    private String projectId;
    private Long startTime;
    private Long updateTime;
    private String currentStatus;

    /**
     * 更新任务状态
     *
     * @param currentStatus currentStatus
     */
    public void updateCurrentStatus(String currentStatus) {
        this.updateTime = ToolUtil.getMillionSeconds();
        this.currentStatus = currentStatus;
    }

    /**
     * 判断任务是否过期
     *
     * @return isExpired
     */
    public boolean isExpired() {
        if (Constants.PERFORMANCE_TESTING.equals(currentStatus)) {
            return (ToolUtil.getMillionSeconds() - this.updateTime) > PERFORMANCE_DELAY;
        } else {
            return (ToolUtil.getMillionSeconds() - this.updateTime) > DELAY;
        }
    }
}
