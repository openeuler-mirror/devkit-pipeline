/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AESUtil
 *
 * @author liuchunwang
 * @since 2023-07-21
 */
@Slf4j
@Component
public class AESUtil {
    /**
     * CRYPTO
     */
    public static final Crypto CRYPTO = new Crypto();

    private static final int PRIVATE_KEY_LENGTH = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final Environment environment;

    /**
     * AESUtil
     *
     * @param environment env
     */
    public AESUtil(Environment environment) {
        this.environment = environment;
    }

    /**
     * Crypto
     *
     * @author liuchunwang
     * @since 2023-07-21
     */
    @Data
    public static class Crypto {
        private String aesCipherMode;
        private String aesDefaultKey;
        private String aesDefaultAad;
    }

    @PostConstruct
    private void initCrypto() {
        CRYPTO.aesCipherMode = environment.getRequiredProperty("crypto.aes-cipher-mode");
        CRYPTO.aesDefaultKey = environment.getRequiredProperty("crypto.aes-default-key");
        CRYPTO.aesDefaultAad = environment.getRequiredProperty("crypto.aes-default-aad");
    }

    /**
     * get PrivateKey
     *
     * @return PrivateKey
     */
    public static byte[] getPrivateKey() {
        KeyGenerator keygen = null;
        try {
            keygen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }

        if (keygen != null) {
            keygen.init(AESUtil.PRIVATE_KEY_LENGTH);
            return keygen.generateKey().getEncoded();
        }
        return new byte[0];
    }

    /**
     * gcmEncrypt
     *
     * @param input input
     * @param key key
     * @param aadStr aadStr
     * @return String
     */
    public static String gcmEncrypt(String input, String key, String aadStr) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] privateKey = hex2byte(key);
        byte[] iv = genRandomBytes();
        int tagLength = GCM_TAG_LENGTH;
        byte[] aad = StringUtils.isBlank(aadStr) ? null : aadStr.getBytes(StandardCharsets.UTF_8);
        byte[] cipherData = Optional.ofNullable(gcmEncrypt(data, privateKey, iv, tagLength, aad))
                .orElseThrow(() -> new BaseException("Parsing exceptions"));

        byte[] ivLengthByteArray = int2ByteArray(iv.length);
        byte[] tagLengthByteArray = int2ByteArray(tagLength);
        int aadLength = aad == null ? 0 : aad.length;
        byte[] addLengthByteArray = int2ByteArray(aadLength);

        byte[] result = new byte[12 + iv.length + aadLength + cipherData.length];
        System.arraycopy(ivLengthByteArray, 0, result, 0, 4);
        System.arraycopy(tagLengthByteArray, 0, result, 4, 4);
        System.arraycopy(addLengthByteArray, 0, result, 8, 4);
        System.arraycopy(iv, 0, result, 12, iv.length);
        if (aad != null) {
            System.arraycopy(aad, 0, result, 12 + iv.length, aadLength);
        }
        System.arraycopy(cipherData, 0, result, 12 + iv.length + aadLength, cipherData.length);
        return byte2Hex(result);
    }

    /**
     * gcmDecrypt
     *
     * @param input input
     * @param key key
     * @return String
     */
    public static String gcmDecrypt(String input, String key) {
        byte[] data = hex2byte(input);
        byte[] paramLength = new byte[4];

        System.arraycopy(data, 0, paramLength, 0, 4);
        int ivLength = byteArray2Int(paramLength);
        byte[] iv = new byte[ivLength];
        System.arraycopy(data, 12, iv, 0, ivLength);
        System.arraycopy(data, 4, paramLength, 0, 4);
        int tagLength = byteArray2Int(paramLength);
        System.arraycopy(data, 8, paramLength, 0, 4);

        int aadLength = byteArray2Int(paramLength);
        byte[] aad = aadLength == 0 ? null : new byte[aadLength];
        if (aad != null) {
            System.arraycopy(data, 12 + ivLength, aad, 0, aadLength);
        }

        byte[] cipherData = new byte[data.length - ivLength - aadLength - 12];
        System.arraycopy(data, 12 + ivLength + aadLength, cipherData, 0, cipherData.length);

        byte[] privateKey = hex2byte(key);
        return new String(gcmDecrypt(cipherData, privateKey, iv, tagLength, aad));
    }

    private static byte[] gcmEncrypt(byte[] data, byte[] privateKey, byte[] iv, int tagLength, byte[] aad) {
        try {
            Cipher cipher = cipher(privateKey, iv, tagLength, Cipher.ENCRYPT_MODE);
            return handle(data, cipher, aad);
        } catch (GeneralSecurityException e) {
            throw new BaseException("Parsing exceptions");
        }
    }

    private static byte[] gcmDecrypt(byte[] data, byte[] privateKey, byte[] iv, int tagLength, byte[] aad) {
        try {
            Cipher cipher = cipher(privateKey, iv, tagLength, Cipher.DECRYPT_MODE);
            return handle(data, cipher, aad);
        } catch (GeneralSecurityException e) {
            throw new BaseException("Parsing exceptions");
        }
    }

    private static Cipher cipher(byte[] privateKey, byte[] iv, int tagLength, int mode)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(CRYPTO.aesCipherMode);
        SecretKey secretKey = new SecretKeySpec(privateKey, "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(tagLength * Byte.SIZE, iv);
        cipher.init(mode, secretKey, parameterSpec);
        return cipher;
    }

    private static byte[] handle(byte[] data, Cipher cipher, byte[] aad) throws GeneralSecurityException {
        if (aad != null) {
            cipher.updateAAD(aad);
        }
        return cipher.doFinal(data);
    }

    private static byte[] genRandomBytes() {
        byte[] result = new byte[AESUtil.GCM_IV_LENGTH];
        SecureRandom rand = RandomUtil.getRandom();
        rand.nextBytes(result);
        return result;
    }

    private static byte[] int2ByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    private static int byteArray2Int(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            result += (bytes[i] & 0xFF) << shift;
        }
        return result;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(16);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex.toUpperCase(Locale.US));
        }
        return sb.toString();
    }

    private static byte[] hex2byte(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] =
                    (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                            + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }
}
