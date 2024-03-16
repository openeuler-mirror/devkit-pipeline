/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.util.Constants;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AbsParsePerformanceFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
abstract class AbsParsePerformanceFiles {
    private final Map<String, byte[]> inputByteMap;

    /**
     * Construction function
     *
     * @param inputByteMap map
     */
    public AbsParsePerformanceFiles(Map<String, byte[]> inputByteMap) {
        this.inputByteMap = inputByteMap;
    }

    /**
     * Get result
     *
     * @param testName testName
     * @param fileType file Type
     * @param fileTypeEn file type in English
     * @param descIndex description index
     * @return CompatibilityTestResult object
     * @throws IOException IOException
     */
    public CompatibilityTestResult getResult(String testName, String fileType, String fileTypeEn, int descIndex)
            throws IOException {
        CompatibilityTestResult testResult = new CompatibilityTestResult(testName);
        if (inputByteMap == null || inputByteMap.size() == 0) {
            throw new BaseException(String.format(Locale.ROOT, "解析压力测试%s文件缺失", fileType));
        }
        List<String> beginFileNames = inputByteMap.keySet().stream().filter(s -> StringUtils.endsWith(s, ".log"))
                .collect(Collectors.toList());
        if (beginFileNames.size() > 0) {
            return readFile(inputByteMap.get(beginFileNames.get(0)));
        }

        testResult.setResult(Constants.TEST_FAILED);
        testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(descIndex), fileType));
        testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(descIndex),
                fileTypeEn));
        return testResult;
    }

    /**
     * read file
     *
     * @param fileBytes file bytes
     * @return CompatibilityTestResult
     * @throws IOException Exception
     */
    abstract CompatibilityTestResult readFile(byte[] fileBytes) throws IOException;

    /**
     * Get Idle list
     *
     * @param fileBytes FILE BYTES
     * @return IDLE List
     * @throws IOException exception
     */
    abstract List<String> getIdleList(byte[] fileBytes) throws IOException;

    /**
     * GET idle map
     *
     * @param fileBytes fileBytes
     * @return idle map
     * @throws IOException exception
     */
    abstract Map<String, Map<String, List<String>>> getIdleMap(byte[] fileBytes) throws IOException;
}
