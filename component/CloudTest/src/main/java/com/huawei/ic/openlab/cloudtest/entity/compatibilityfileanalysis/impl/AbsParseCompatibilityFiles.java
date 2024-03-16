/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.util.Constants;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AbsParseCompatibilityFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
abstract class AbsParseCompatibilityFiles {
    private final Map<String, byte[]> beginByteMap;
    private final Map<String, byte[]> endByteMap;

    /**
     * AbsParseCompatibilityFiles init
     *
     * @param beginByteMap beginByteMap
     * @param endByteMap endByteMap
     */
    protected AbsParseCompatibilityFiles(Map<String, byte[]> beginByteMap, Map<String, byte[]> endByteMap) {
        this.beginByteMap = beginByteMap;
        this.endByteMap = endByteMap;
    }

    /**
     * read File
     *
     * @param beginBytes beginBytes
     * @param endBytes endBytes
     * @return CompatibilityTestResult
     * @throws IOException Exception
     */
    abstract CompatibilityTestResult readFile(byte[] beginBytes, byte[] endBytes) throws IOException;

    /**
     * Get average rate
     *
     * @param fileBytes file bytes
     * @return average list
     * @throws IOException exception
     */
    abstract List<String> getAverage(byte[] fileBytes) throws IOException;

    /**
     * Get Result
     *
     * @param testName Test name
     * @param fileType file type
     * @param fileTypeEn file type in English
     * @param descIndex description index
     * @return CompatibilityTestResult
     * @throws IOException exception
     */
    public CompatibilityTestResult getResult(String testName, String fileType, String fileTypeEn, int descIndex)
            throws IOException {
        if (beginByteMap == null || beginByteMap.size() == 0) {
            throw new BaseException(String.format(Locale.ROOT, "解析空载测试%s文件缺失", fileType));
        }
        if (endByteMap == null || endByteMap.size() == 0) {
            throw new BaseException(String.format(Locale.ROOT, "解析空载测试%s文件缺失", fileType));
        }
        List<CompatibilityTestResult> clusterResult = new ArrayList<>();
        List<String> beginFileNames = beginByteMap.keySet().stream().filter(s -> StringUtils.endsWith(s, ".log"))
                .collect(Collectors.toList());
        List<String> endFileNames = endByteMap.keySet().stream().filter(s -> StringUtils.endsWith(s, ".log"))
                .collect(Collectors.toList());

        if (beginFileNames.size() > 0 && endFileNames.size() > 0) {
            clusterResult.add(readFile(beginByteMap.get(beginFileNames.get(0)), endByteMap.get(endFileNames.get(0))));
        }

        if (clusterResult.size() > 0) {
            if (clusterResult.stream().anyMatch(s -> StringUtils.contains(s.getResult(), Constants.TEST_FAILED))) {
                return clusterResult.stream().filter(s -> StringUtils.contains(s.getResult(), Constants.TEST_FAILED))
                        .collect(Collectors.toList()).get(0);
            } else {
                return clusterResult.get(0);
            }
        }
        CompatibilityTestResult testResult = new CompatibilityTestResult(testName);
        testResult.setResult(Constants.TEST_FAILED);
        testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(descIndex), fileType));
        testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(descIndex),
                fileTypeEn));
        return testResult;
    }
}
