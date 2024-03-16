/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.dao;

import com.huawei.ic.openlab.cloudtest.entity.CloudLabTestTask;
import com.huawei.ic.openlab.cloudtest.entity.LabTestReq;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CloudLabTestTaskDao
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */

public interface CloudLabTestTaskDao {
    /**
     * get Test Task
     *
     * @param projectId projectId
     * @return CloudLabTestTask
     */
    CloudLabTestTask getTestTask(@Param("projectId") String projectId);

    /**
     * get Ongoing Task
     *
     * @param data LabTestReq
     * @return Task
     */
    List<String> getOngoingTask(@Param("data") LabTestReq data);

    /**
     * set Test Task
     *
     * @param projectId projectId
     * @param data data
     */
    void setTestTask(@Param("projectId") String projectId,
                     @Param("data") CloudLabTestTask data);

    /**
     * update Project Status
     *
     * @param projectId projectId
     * @param status status
     * @param startTime start Time
     * @param finishTime finish Time
     * @param statusDesc status Desc
     * @return update num
     */
    int updateProjectStatus(@Param("projectId") String projectId,
                            @Param("status") Integer status,
                            @Param("startTime") String startTime,
                            @Param("finishTime") String finishTime,
                            @Param("statusDesc") String statusDesc);

    /**
     * update Upload File
     *
     * @param projectId projectId
     * @param userId userId
     * @param column column
     * @param data data
     * @param ip ip
     * @return update num
     */
    int updateUploadFile(@Param("projectId") String projectId,
                         @Param("userId") String userId,
                         @Param("column") String column,
                         @Param("data") CloudLabTestTask.UploadFile data,
                         @Param("ip") String ip);

    /**
     * update Step Status
     *
     * @param data data
     * @param testBeginTime test Begin Time
     * @param projectId projectId
     * @return update num
     */
    int updateStepStatus(@Param("data") CloudLabTestTask.StepStatus data,
                         @Param("testBeginTime") CloudLabTestTask.TestBeginTime testBeginTime,
                         @Param("projectId") String projectId);

    /**
     * update With Exception File
     *
     * @param data CloudLabTestTask
     * @return update num
     */
    int updateWithExceptionFile(@Param("data") CloudLabTestTask data);

    /**
     * update Compatibility Result
     *
     * @param data CloudLabTestTask
     * @return update num
     */
    int updateCompatibilityResult(@Param("data") CloudLabTestTask data);

    /**
     * update Performance Result
     *
     * @param data CloudLabTestTask
     * @return update num
     */
    int updatePerformanceResult(@Param("data") CloudLabTestTask data);
}
