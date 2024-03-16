/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.service;

import com.huawei.ic.openlab.cloudtest.common.exception.SshErrorException;
import com.huawei.ic.openlab.cloudtest.dao.CloudLabTestTaskDao;
import com.huawei.ic.openlab.cloudtest.entity.CloudLabTestTask;
import com.huawei.ic.openlab.cloudtest.entity.MqsMessage;
import com.huawei.ic.openlab.cloudtest.entity.SystemParams;
import com.huawei.ic.openlab.cloudtest.entity.TaskDelay;
import com.huawei.ic.openlab.cloudtest.util.Constants;
import com.huawei.ic.openlab.cloudtest.util.ToolUtil;
import com.huawei.ic.openlab.cloudtest.util.sshclient.SSHUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TaskDelayMapService
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
@Slf4j
public class TaskDelayMapService {
    private final Map<String, TaskDelay> taskMap = new HashMap<>();
    private final CloudLabTestTaskDao dao;
    private final SystemParams systemParams;

    /**
     * construction function
     *
     * @param dao dao
     * @param systemParams system parameter
     */
    public TaskDelayMapService(CloudLabTestTaskDao dao, SystemParams systemParams) {
        this.dao = dao;
        this.systemParams = systemParams;
    }

    /**
     * add task
     *
     * @param projectId project id
     * @param currentStatus status
     */
    public void addTask(String projectId, String currentStatus) {
        TaskDelay taskDelay;
        if (taskMap.containsKey(projectId)) {
            taskDelay = taskMap.get(projectId);
            taskDelay.updateCurrentStatus(currentStatus);
        } else {
            taskDelay = new TaskDelay(projectId, ToolUtil.getMillionSeconds(), ToolUtil.getMillionSeconds(),
                    currentStatus);
        }
        taskMap.put(projectId, taskDelay);
    }

    /**
     * update task
     *
     * @param projectId project id
     * @param currentStatus current status
     * @return boolean
     */
    public boolean updateTask(String projectId, String currentStatus) {
        if (taskMap.containsKey(projectId)) {
            TaskDelay taskDelay = taskMap.get(projectId);
            taskDelay.updateCurrentStatus(currentStatus);
            taskMap.put(projectId, taskDelay);
            return true;
        }
        return false;
    }

    /**
     * get task status
     *
     * @param projectId project id
     * @return status
     */
    public String getTaskStatus(String projectId) {
        if (taskMap.containsKey(projectId)) {
            taskMap.get(projectId);
            return taskMap.get(projectId).getCurrentStatus();
        }
        return StringUtils.EMPTY;
    }

    /**
     * remove task
     *
     * @param projectId project id
     * @return remove task
     */
    public boolean removeTask(String projectId) {
        if (taskMap.containsKey(projectId)) {
            taskMap.remove(projectId);
            return true;
        }
        return false;
    }

    /**
     * expire task
     *
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void expireTask() {
        TaskDelay task;
        Iterator<Map.Entry<String, TaskDelay>> iterator = taskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TaskDelay> entry = iterator.next();
            task = entry.getValue();
            if (task.isExpired()) {
                iterator.remove();
                CloudLabTestTask testTask = dao.getTestTask(task.getProjectId());
                testTask.setProjectStatus(Constants.CLOUD_LAB_TEST_EXCEPTION);
                testTask.setStatusDesc(Constants.TASK_EXPIRE_ESC_MAP.get(testTask.getTaskLanguage()));
                testTask.setFinishTime(ToolUtil.getStandardTime());
                dao.updateProjectStatus(testTask.getProjectId(), testTask.getProjectStatus(), "",
                        testTask.getFinishTime(), testTask.getStatusDesc());
                sendExceptionMessage(testTask);

                // 删除服务器上的测试文件
                deleteTestFile(testTask);
                log.error("Task {} status {}: wait for 60 minutes without any change in status, abnormal testing task",
                        task.getProjectId(), task.getCurrentStatus());
            }
        }
    }

    private void sendExceptionMessage(CloudLabTestTask task) {
        MqsMessage message = new MqsMessage();
        message.setProjectId(task.getProjectId());
        message.setUserId(task.getUserId());
        message.setServerIp(task.getServerIp());
        message.setStatusTime(task.getFinishTime());
        message.setStatus(Constants.MQS_STATUS_EXCEPTION);
        message.setStatusDesc(task.getStatusDesc());
    }

    /**
     * delete test file
     *
     * @param task task
     */
    public void deleteTestFile(CloudLabTestTask task) {
        try {
            List<String> commandList = new ArrayList<>();
            commandList.add("cd /home/compatibility_testing;find . -name \"*.sh\"| xargs rm -rf ; "
                    + "rm -rf compatibility_testing.tar.gz;");
            commandList.add("cd /home/function_testing; rm -rf shunit2-master.zip shunit2-master;");

            SSHUtil.sshExecCmd(task.getServerIp(), task.getServerPort(), task.getServerUser(),
                    task.getServerPassword(), commandList);
        } catch (SshErrorException ex) {
            log.error("task {} in server {} failed in delete {}", task.getProjectId(), task.getServerIp(),
                    ex.getLocalizedMessage());
        }
    }
}
