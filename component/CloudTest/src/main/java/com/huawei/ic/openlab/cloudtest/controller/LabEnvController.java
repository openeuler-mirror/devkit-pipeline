/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.controller;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.service.LabEnvService;
import com.huawei.ic.openlab.cloudtest.service.TarFileService;
import com.huawei.ic.openlab.cloudtest.util.Constants;
import com.huawei.ic.openlab.cloudtest.util.NormalResp;

import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 实验室环境测试控制器类
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class LabEnvController {
    private final TarFileService tarFileService;
    private final LabEnvService labEnvService;


    /**
     * LabEnvController
     * @param tarFileService labEnvService
     * @param service LabEnvService
     */
    @Autowired
    public LabEnvController(TarFileService tarFileService, LabEnvService labEnvService) {
        this.tarFileService = tarFileService;
        this.labEnvService = labEnvService;
    }

    /**
     * 测试远程实验室连接是否正常
     *
     * @param ip ip
     * @param port port
     * @param userName username
     * @param passWord password
     * @return response string
     */
    @GetMapping(value = "/report")
    public String setTestFile(@RequestParam(value = "file") String file, @RequestParam(value = "savePath") String savePath) throws IOException {
        if (!Objects.requireNonNull(file).matches("^.*\\.tar.gz$")) {
            throw new BaseException("文件后缀错误");
        }
        labEnvService.resultJson(file, savePath);
        return "success";
    }
}