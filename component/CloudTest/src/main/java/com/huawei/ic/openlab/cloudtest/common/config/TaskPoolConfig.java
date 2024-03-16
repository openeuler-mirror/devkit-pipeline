/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.common.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * TaskPoolConfig
 *
 * @author kongcaizhi
 * @since 2022-10-19
 */
@Configuration
@EnableAsync
@Slf4j
public class TaskPoolConfig implements AsyncConfigurer {
    @Override
    @Bean(name = "asyncExecutor", destroyMethod = "shutdown")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(100);

        // 最大线程数,默认 40000
        executor.setMaxPoolSize(300);

        // 线程池队列最大线程数,默认80000
        executor.setQueueCapacity(1000);

        // 线程池线程最大空闲时间（秒）
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) ->
                log.error(
                        "An exception occurred during asynchronous task execution, message {}, method {}, params {}",
                        throwable.getLocalizedMessage(),
                        method,
                        objects
                );
    }
}
