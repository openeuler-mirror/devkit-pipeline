/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PropertiesUtils
 *
 * @since 2024-07-11
 */
@Slf4j
public class PropertiesUtils {

    public static String ROOT_DIR;

    public static Properties loadProperties(String propertiesFile) {
        Properties properties = new Properties();
        try (InputStream in = Resources.getResourceAsStream(propertiesFile)) {
            properties.load(in);
        } catch (IOException ex) {
            log.error("load properties", ex);
        }
        return properties;
    }

    public static void configAndUpdate(Properties properties) {
        String rootDir = System.getProperty("CODE_INSPECTOR_APP_HOME");
        if (rootDir != null && !rootDir.isEmpty()) {
            ROOT_DIR = rootDir;
            properties.setProperty("root.dir", ROOT_DIR);
            return;
        }
        String mode = properties.getProperty("mode");
        if ("dev".equals(mode)) {
            ROOT_DIR = properties.getProperty("root.dir");
        } else {
            ROOT_DIR = System.getProperty("user.dir");
            properties.setProperty("root.dir", ROOT_DIR);
        }
    }

}
