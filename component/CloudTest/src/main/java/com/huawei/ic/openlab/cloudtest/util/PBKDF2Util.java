/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Pbkdf2Util
 *
 * @since 2022-11-08
 * @author kongcaizhi
 */
@Slf4j
public class PBKDF2Util {
    private static final String DEFAULT_CIPHER_AND_PADDING = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 10000000;
    private static final int KEY_LENGTH = 256;
    private static final int COMPONENT_LENGTH = 48;

    private static byte[] digest(char[] password, String salt) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DEFAULT_CIPHER_AND_PADDING);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt.getBytes(Charset.defaultCharset()), ITERATION_COUNT,
                KEY_LENGTH);
        SecretKey secretKey = keyFactory.generateSecret(pbeKeySpec);
        return secretKey.getEncoded();
    }

    /**
     * pbkdf2ForRootKey
     *
     * @param salt salt
     * @param components components
     * @return String
     */
    public static String pbkdf2ForRootKey(String salt, String... components) {
        char[] component = new char[COMPONENT_LENGTH];
        for (int i = 0; i < COMPONENT_LENGTH; i++) {
            component[i] = 0;
            for (String s : components) {
                char[] temp = s.toCharArray();
                component[i] ^= temp[i];
            }
        }

        try {
            return new String(PBKDF2Util.digest(component, salt), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
            throw new BaseException(e.getMessage());
        }
    }
}
