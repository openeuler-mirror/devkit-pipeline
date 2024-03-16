/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

/**
 * NormalResp
 *
 * @author kongcaizhi
 * @since 2021-03-23
 */
@FeignClient(url = "EMPTY", name = "PerformanceTest")
public interface PerformanceApiClient {
    /**
     * set Performance Test
     *
     * @param uri uri
     * @param projectId projectId
     * @param serverIp serverIp
     * @param isTimeLimit isTimeLimit
     * @param multipartFile multipartFile
     * @return result
     */
    @PostMapping(
            value = "/{projectId}/performance-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String setPerformanceTest(URI uri, @PathVariable("projectId") String projectId,
                              @RequestParam("serverIp") String serverIp,
                              @RequestParam("timeLimit") boolean isTimeLimit,
                              @RequestPart(value = "file") MultipartFile multipartFile);

    /**
     * stop Performance Test
     *
     * @param uri uri
     * @param projectId projectId
     * @return result
     */
    @GetMapping("/{projectId}/stop-test")
    String stopPerformanceTest(URI uri, @PathVariable("projectId") String projectId);
}
