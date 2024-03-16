/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 云开发平台访问记录
 *
 * @since 2023-01-16
 * @author kongcaizhi
 */

public interface CloudDevUseRecordDao {
    /**
     * Get record by taskId
     *
     * @param taskDeviceId task id
     * @param kpToken kp token
     * @return response string
     */
    String getRecordById(@Param("taskDeviceId") String taskDeviceId,
                         @Param("kpToken") String kpToken);

    /**
     * insert record
     *
     * @param requestTime requestTime
     * @param taskDeviceId taskDeviceId
     * @param kpToken KP TOKEN
     * @return RESPONSE STRING
     */
    int insertRecord(@Param("requestTime") String requestTime,
                     @Param("taskDeviceId") String taskDeviceId,
                     @Param("kpToken") String kpToken);
}
