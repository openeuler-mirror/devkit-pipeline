/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.service;

import com.huawei.ic.openlab.cloudtest.entity.SystemParams;
import com.huawei.ic.openlab.cloudtest.util.ApiCenterUtil;
import com.huawei.ic.openlab.cloudtest.util.DgCodeServerClient;
import com.huawei.ic.openlab.cloudtest.util.TeeCodeServerClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

/**
 * devkit环境申请service
 *
 * @author kongcaizhi
 * @since 2022-10-19
 */
@Slf4j
public class DevkitService {
    private final TeeCodeServerClient client;
    private final DgCodeServerClient dgCodeServerClient;
    private final SystemParams systemParams;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * construction function
     *
     * @param client client
     * @param dgCodeServerClient dgCodeServer client
     * @param systemParams system parameter
     */
    public DevkitService(TeeCodeServerClient client, DgCodeServerClient dgCodeServerClient,
                         SystemParams systemParams) {
        this.client = client;
        this.dgCodeServerClient = dgCodeServerClient;
        this.systemParams = systemParams;
    }

    /**
     * 往redis 增加 device-id
     *
     * @param server 服务器信息
     */
    public void addTaskDeviceId(DevkitServer server) {
        client.addTaskDeviceId(server.getTaskDeviceId(), server.getInternalIp());
        dgCodeServerClient.addTaskDeviceId(server.getTaskDeviceId(), server.getInternalIp());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(server.getTaskDeviceId()))) {
            redisTemplate.opsForValue().setIfAbsent(server.getTaskDeviceId(), server.getInternalIp());
        } else {
            redisTemplate.opsForValue().append(server.getTaskDeviceId(), server.getInternalIp());
        }
    }

    /**
     * 往redis 删除device-id
     *
     * @param taskDeviceId device-id
     * @return boolean
     */
    public boolean deleteTaskDeviceId(String taskDeviceId) {
        client.deleteTaskDeviceId(taskDeviceId);
        dgCodeServerClient.deleteTaskDeviceId(taskDeviceId);
        return Boolean.TRUE.equals(redisTemplate.delete(taskDeviceId));
    }

    /**
     * 向API网关验证token是否有效
     *
     * @return validate
     */
    public boolean validateToken() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes instanceof ServletRequestAttributes
                ? ((ServletRequestAttributes) requestAttributes).getRequest() : null;

        if (request == null) {
            return false;
        }

        String token = request.getHeader("KP-TOKEN");
        String taskDeviceId = request.getHeader("TASK-DEVICE-ID");

        if (StringUtils.isEmpty(token)) {
            log.error("token is empty");
            return false;
        }

        if (StringUtils.isEmpty(taskDeviceId)) {
            log.error("taskDeviceId is empty");
            return false;
        }
        return validateToken(token, taskDeviceId);
    }

    /**
     * 检查redis是否有token, 如果有,返回为真,如果没有请求apigw判断.如果apigw返回为真,则保存在redis中,设置超时时间为1分钟.
     *
     * @param token token
     * @param taskDeviceId taskDeviceId
     * @return true|false
     */
    private boolean validateToken(String token, String taskDeviceId) {
        String key = token + "-" + taskDeviceId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return true;
        } else {
            boolean isValidated = ApiCenterUtil.validateToken(systemParams, token, taskDeviceId);
            if (isValidated) {
                redisTemplate.opsForValue().set(key, "token", 5, TimeUnit.MINUTES);
                log.info("Add token to redis return{}",
                        redisTemplate.expire(key, 5, TimeUnit.MINUTES));
            }
            return isValidated;
        }
    }

    /**
     * DevkitServer
     *
     * @author kongcaizhi
     * @since 2022-10-19
     */
    @Data
    public static class DevkitServer {
        private String taskDeviceId;
        private String agentId;
        private String internalIp;
    }
}
