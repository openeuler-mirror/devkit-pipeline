/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ToolUtil
 *
 * @author kongcaizhi
 * @since 2021-03-23
 */
public class ToolUtil {
    private static final DateTimeFormatter STRING_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
    private static final String REGEX = "\\.\\.+|\\/[.|\\\\]\\/";
    private static final String REGEX_URL = "([0-9]+\\.){3}[0-9]+";

    /**
     * get Time Str
     *
     * @return Time Str
     */
    public static String getTimeStr() {
        return LocalDateTime.now().format(STRING_FORMATTER);
    }

    /**
     * get Standard Time
     *
     * @return Standard Time
     */
    public static String getStandardTime() {
        return LocalDateTime.now().format(STANDARD_FORMATTER);
    }

    /**
     * get data Time
     *
     * @return data Time
     */
    public static String getDateStr() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }

    /**
     * getMillionSeconds
     *
     * @return MillionSeconds
     */
    public static Long getMillionSeconds() {
        return System.currentTimeMillis();
    }

    /**
     * validPath
     *
     * @param path path
     */
    public static void validPath(String path) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            throw new BaseException("The File's path is illegal: " + path);
        }
    }

    /**
     * checkParameter
     *
     * @param param param
     */
    public static void checkParameter(String param) {
        Pattern pattern = Pattern.compile(REGEX_URL);
        Matcher matcher = pattern.matcher(param);
        if (!matcher.find()) {
            throw new BaseException("The URL's param is illegal: " + param);
        }
    }
}
