/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.common.exception;

/**
 * ClamAVSizeLimitException
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
public class ClamAvSizeLimitException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Construction function
     *
     * @param msg message
     */
    public ClamAvSizeLimitException(String msg) {
        super(msg);
    }
}
