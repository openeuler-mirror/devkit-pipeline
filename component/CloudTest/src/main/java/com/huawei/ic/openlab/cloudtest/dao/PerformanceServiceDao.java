/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.dao;

import com.huawei.ic.openlab.cloudtest.entity.PerformanceService;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PerformanceServiceDao
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */

public interface PerformanceServiceDao {
    /**
     * Get Performance service
     *
     * @return get record
     */
    List<PerformanceService> getPerformanceService();

    /**
     * Get Idle Service
     *
     * @return get record
     */
    PerformanceService getIdleService();

    /**
     * add task
     *
     * @param serviceIp service ip
     * @return add record
     */
    int addTask(@Param("serviceIp") String serviceIp);

    /**
     * Subtract task
     *
     * @param serviceIp service IP
     * @return record
     */
    int subtractTask(@Param("serviceIp") String serviceIp);
}
