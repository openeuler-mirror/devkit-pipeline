/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * RandomUtil
 *
 * @author liuchunwang
 * @since 2023-11-03
 */
public class RandomUtil {
    /**
     * getRandom
     *
     * @return SecureRandom
     */
    public static SecureRandom getRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new BaseException("make random number exception");
        }
    }
}
