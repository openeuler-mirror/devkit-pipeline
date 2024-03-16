/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.common.exception;

/**
 * BaseException
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 120492404095565805L;

    /**
     * BaseException
     *
     * @param message message
     */
    public BaseException(String message) {
        super(message);
    }
}
