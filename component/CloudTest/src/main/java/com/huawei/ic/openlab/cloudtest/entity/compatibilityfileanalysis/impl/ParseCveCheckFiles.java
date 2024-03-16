/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityFilesParser;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.util.Constants;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ParseCveCheckFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseCveCheckFiles implements CompatibilityFilesParser {
    private final Map<String, byte[]> inputByteMap;

    /**
     * construction function
     *
     * @param inputByteMap map
     */
    public ParseCveCheckFiles(Map<String, byte[]> inputByteMap) {
        this.inputByteMap = inputByteMap;
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SECURITY_VULNERABLE.getTestName());
        if (inputByteMap == null || inputByteMap.size() == 0) {
            throw new BaseException("漏洞扫描文件缺失");
        }
        List<String> beginFileNames = inputByteMap.keySet().stream().filter(s -> StringUtils.contains(s,
                        "cvecheck-result.json"))
                .collect(Collectors.toList());
        if (beginFileNames.size() > 0) {
            return readVirusScanFile(inputByteMap.get(beginFileNames.get(0)));
        }
        testResult.setResult(Constants.TEST_FAILED);
        testResult.setReason(Constants.SAFETY_FILE_LOSS);
        testResult.setReasonEn(Constants.SAFETY_FILE_LOSS_EN);
        return testResult;
    }

    /**
     * readVirusScanFile
     *
     * @param cveFileBytes cveFileBytes
     * @return CompatibilityTestResult
     * @throws IOException IOException
     */
    public CompatibilityTestResult readVirusScanFile(byte[] cveFileBytes) throws IOException {
        if (cveFileBytes.length == 0) {
            throw new BaseException("漏洞扫描文件缺失");
        }
        String line;
        StringBuilder stringBuilder = new StringBuilder(16);
        try (InputStream inputStream = new ByteArrayInputStream(cveFileBytes);
             BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SECURITY_VULNERABLE.getTestName());
        if (stringBuilder.toString().matches("(\\[\\])+")) {
            testResult.setResult(Constants.TEST_PASSED);
        } else {
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setEvidence(Collections.singletonList(stringBuilder.toString()));
            testResult.setEvidenceEn(Collections.singletonList(stringBuilder.toString()));
            testResult.setReason(stringBuilder.toString());
            testResult.setReasonEn(stringBuilder.toString());
        }
        return testResult;
    }

    @Override
    public CompatibilityTestResult parseFiles() throws IOException {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SECURITY_VULNERABLE.getTestName());
        try {
            testResult = getResult();
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception in parsing cve check file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SAFETY_FILE_LOSS);
            testResult.setReasonEn(Constants.SAFETY_FILE_LOSS_EN);
        }
        return testResult;
    }
}
