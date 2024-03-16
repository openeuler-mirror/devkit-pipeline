/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CompatibleTestApplication
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAsync
@EnableScheduling
@EnableFeignClients
public class CompatibleTestApplication {
    /**
     * main
     *
     * @param args params
     */
    public static void main(String[] args) {
        SpringApplication.run(CompatibleTestApplication.class, args);
    }
}
