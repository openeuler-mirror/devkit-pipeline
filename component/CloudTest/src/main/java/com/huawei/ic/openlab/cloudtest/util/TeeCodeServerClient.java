/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TeeCodeServer nginx动态路由增加和删除taskDeviceId
 *
 * @author kongcaizhi
 * @since 2022-12-20
 */
@FeignClient(url = "${teeCodeServerUrl}", name = "TeeCodeServer")
public interface TeeCodeServerClient {
    /**
     * 路由增加taskDeviceId
     *
     * @param taskDeviceId 服务器信息
     * @param internalIp 服务器信息
     */
    @GetMapping("/add-task-device-id")
    void addTaskDeviceId(@RequestParam("taskDeviceId") String taskDeviceId,
                         @RequestParam("internalIp") String internalIp);

    /**
     * 路由删除taskDeviceId
     *
     * @param taskDeviceId taskdeviceId
     */
    @GetMapping("/delete-task-device-id")
    void deleteTaskDeviceId(@RequestParam("taskDeviceId") String taskDeviceId);
}
