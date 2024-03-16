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
public class SshErrorException extends Exception {
    private static final long serialVersionUID = 116717817166294456L;

    /**
     * SshErrorException
     *
     * @param message message
     */
    public SshErrorException(String message) {
        super(message);
    }
}
