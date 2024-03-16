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
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AbsParsePerformanceFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseClamLog implements CompatibilityFilesParser {
    private static final String RESULT_DESC = "result";
    private static final String EVIDENCE_DESC = "evidence";

    private final Map<String, byte[]> inputByteMap;

    /**
     * Construction function
     *
     * @param inputByteMap map
     */
    public ParseClamLog(Map<String, byte[]> inputByteMap) {
        this.inputByteMap = inputByteMap;
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SECURITY_VIRUS.getTestName());
        if (inputByteMap == null || inputByteMap.size() == 0) {
            throw new BaseException("病毒扫描文件缺失");
        }
        List<String> beginFileNames = inputByteMap.keySet().stream().filter(s -> StringUtils.contains(s,
                        "clam.log"))
                .collect(Collectors.toList());
        if (beginFileNames.size() > 0) {
            return readVirusScanFile(inputByteMap.get(beginFileNames.get(0)));
        }
        testResult.setResult(Constants.TEST_FAILED);
        testResult.setReason(Constants.SAFETY_FILE_LOSS);
        testResult.setReasonEn(Constants.SAFETY_FILE_LOSS_EN);
        return testResult;
    }

    private Map<String, List<String>> getScanResult(BufferedReader reader) throws IOException {
        String result = null;
        List<String> evidenceList = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String lineTrim = line.trim();
            evidenceList.add(lineTrim);
            if (lineTrim.isEmpty()) {
                break;
            }
            Pattern pattern = Pattern.compile(Constants.CLAM_RESULT_PATTERN);
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                result = matcher.group(1).trim();
            }
        }
        Map<String, List<String>> resultMap = new HashMap<>();
        resultMap.put(RESULT_DESC, Collections.singletonList(result));
        resultMap.put(EVIDENCE_DESC, evidenceList);
        return resultMap;
    }

    /**
     * Read virus scan file
     *
     * @param clamFileBytes clam file bytes
     * @return CompatibilityTestResult
     * @throws IOException exception
     */
    public CompatibilityTestResult readVirusScanFile(byte[] clamFileBytes) throws IOException {
        if (clamFileBytes.length == 0) {
            throw new BaseException("病毒扫描文件的内容为空");
        }

        Map<String, List<String>> resultMap = new HashMap<>();
        try (InputStream inputStream = new ByteArrayInputStream(clamFileBytes);
             BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtils.containsIgnoreCase(line, "SCAN SUMMARY")) {
                    resultMap = getScanResult(bufferedReader);
                }
            }
        }

        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SECURITY_VIRUS.getTestName());
        if (resultMap.containsKey(RESULT_DESC) && !CollectionUtils.isEmpty(resultMap.get(RESULT_DESC))) {
            if (StringUtils.equals(resultMap.get(RESULT_DESC).get(0), "0")) {
                testResult.setResult(Constants.TEST_PASSED);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
            }
            if (resultMap.containsKey(EVIDENCE_DESC) && resultMap.get(EVIDENCE_DESC) != null) {
                testResult.setEvidence(resultMap.get(EVIDENCE_DESC));
                testResult.setEvidenceEn(resultMap.get(EVIDENCE_DESC));
            }
        } else {
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SAFETY_FILE_LOSS);
            testResult.setReasonEn(Constants.SAFETY_FILE_LOSS_EN);
        }
        return testResult;
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult =
                new CompatibilityTestResult(Constants.CompatibilityTestName.SECURITY_VIRUS.getTestName());
        try {
            testResult = getResult();
        } catch (IOException ex) {
            log.error("Exception occur in parsing virus file", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SAFETY_FILE_LOSS);
        }
        return testResult;
    }
}
