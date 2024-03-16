/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis;

import com.huawei.ic.openlab.cloudtest.util.Constants;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

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

/**
 * ParseInfoLog
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseInfoLog {
    private final byte[] infoLogBytes;
    private String osVersion;

    /**
     * construction function
     *
     * @param infoLogBytes info log bytes
     * @param osVersion os version
     */
    public ParseInfoLog(byte[] infoLogBytes, String osVersion) {
        this.infoLogBytes = infoLogBytes;
        this.osVersion = osVersion;
    }

    private Map<String, CompatibilityTestResult> initMap() {
        Map<String, CompatibilityTestResult> resultMap = new HashMap<>();
        resultMap.put(Constants.CompatibilityTestName.APPLICATION_START.getTestName(),
                new CompatibilityTestResult(Constants.CompatibilityTestName.APPLICATION_START.getTestName()));
        resultMap.put(Constants.CompatibilityTestName.APPLICATION_STOP.getTestName(),
                new CompatibilityTestResult(Constants.CompatibilityTestName.APPLICATION_STOP.getTestName()));
        resultMap.put(Constants.CompatibilityTestName.EXCEPTION_KILL.getTestName(),
                new CompatibilityTestResult(Constants.CompatibilityTestName.EXCEPTION_KILL.getTestName()));
        return resultMap;
    }

    /**
     * parse info log
     *
     * @return Map
     */
    public Map<String, CompatibilityTestResult> parseInfoLog() {
        Map<String, CompatibilityTestResult> resultMap = initMap();
        if (infoLogBytes.length == 0) {
            for (Map.Entry<String, CompatibilityTestResult> entry : resultMap.entrySet()) {
                CompatibilityTestResult testResult = entry.getValue();
                testResult.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get("FILE_NOT_EXIT").getTestResult());
                testResult.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get("FILE_NOT_EXIT").getTestReason());
                testResult.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get("FILE_NOT_EXIT").getTestReasonEn());
                entry.setValue(testResult);
            }
            return resultMap;
        }
        try {
            resultMap = readInfoLog();
        } catch (IllegalStateException | IOException ex) {
            log.error("Exception in parsing info.log ", ex);
            for (Map.Entry<String, CompatibilityTestResult> entry : resultMap.entrySet()) {
                CompatibilityTestResult testResult = entry.getValue();
                testResult.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get("READ_ERROR").getTestResult());
                testResult.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get("READ_ERROR").getTestReason());
                testResult.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get("READ_ERROR").getTestReasonEn());
                entry.setValue(testResult);
            }
        } catch (Exception ex) {
            log.error("Exception in uncompressing info.log ", ex);
            for (Map.Entry<String, CompatibilityTestResult> entry : resultMap.entrySet()) {
                CompatibilityTestResult testResult = entry.getValue();
                testResult.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get("UNZIP_ERROR").getTestResult());
                testResult.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get("UNZIP_ERROR").getTestReason());
                testResult.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get("UNZIP_ERROR").getTestReasonEn());
                entry.setValue(testResult);
            }
        }
        return resultMap;
    }

    /**
     * get os version
     *
     * @return os version
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * 读取Info日志
     *
     * @return map
     * @throws IOException exception
     */
    private Map<String, CompatibilityTestResult> readInfoLog() throws IOException {
        Map<String, List<String>> evidenceMap = new HashMap<>();

        try (InputStream inputStream = new ByteArrayInputStream(infoLogBytes);
             BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<Integer> beginTestIndex = new ArrayList<>();
            String line;
            int count = 0;
            reader.mark(infoLogBytes.length + 1);
            while ((line = reader.readLine()) != null) {
                count++;
                if (StringUtils.contains(line, Constants.START_TEST_STRING) || StringUtils.contains(line,
                        Constants.START_TEST_STRING_EN)) {
                    beginTestIndex.add(count);
                }
            }
            int lastBeginTest = Collections.max(beginTestIndex);
            reader.reset();

            for (int i = 0; i <= lastBeginTest; i++) {
                reader.readLine();
            }

            StringBuilder stringBuilder = new StringBuilder(16);
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }

            Matcher osMatcher = Pattern.compile(Constants.OS_PATTERN_CN).matcher(stringBuilder.toString());
            if (osMatcher.find()) {
                osVersion = osMatcher.group(3);
            }
            for (Map.Entry<String, Pattern> entry : Constants.LOG_WHITE_RULES.entrySet()) {
                Pattern pattern = entry.getValue();
                Matcher matcher = pattern.matcher(stringBuilder.toString());
                if (matcher.find()) {
                    List<String> evidenceList = evidenceMap.getOrDefault(entry.getKey(), new ArrayList<>());
                    evidenceList.add(matcher.group(0).replace(System.lineSeparator(), ""));
                    evidenceMap.put(entry.getKey(), evidenceList);
                }
            }
        }
        return getResultMap(evidenceMap);
    }

    /**
     * 根据日志解析内容,返回用例结果.
     *
     * @param evidenceMap map
     * @return map
     */
    private Map<String, CompatibilityTestResult> getResultMap(Map<String, List<String>> evidenceMap) {
        CompatibilityTestResult startTest =
                new CompatibilityTestResult(Constants.CompatibilityTestName.APPLICATION_START.getTestName());
        CompatibilityTestResult stopTest =
                new CompatibilityTestResult(Constants.CompatibilityTestName.APPLICATION_STOP.getTestName());
        CompatibilityTestResult exceptionTest =
                new CompatibilityTestResult(Constants.CompatibilityTestName.EXCEPTION_KILL.getTestName());
        startTest.setOsVersion(getOsVersion());

        if (evidenceMap.size() > 0) {
            for (Map.Entry<String, List<String>> entry : evidenceMap.entrySet()) {
                if (entry.getKey().contains("START_APP")) {
                    startTest.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestResult());
                    startTest.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestReason());
                    startTest.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestReasonEn());
                    startTest.setEvidence(entry.getValue());
                    startTest.setEvidenceEn(entry.getValue());
                }
                if (entry.getKey().contains("RELIABLE_TEST")) {
                    exceptionTest.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestResult());
                    exceptionTest.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestReason());
                    exceptionTest.setReasonEn(
                            Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestReasonEn());
                    exceptionTest.setEvidence(entry.getValue());
                    exceptionTest.setEvidenceEn(entry.getValue());
                }
                if (entry.getKey().contains("STOP_APP")) {
                    stopTest.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestResult());
                    stopTest.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestReason());
                    stopTest.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get(entry.getKey()).getTestReasonEn());
                    stopTest.setEvidence(entry.getValue());
                    stopTest.setEvidenceEn(entry.getValue());
                }
            }
        } else {
            startTest.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_START_APP").getTestResult());
            startTest.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_START_APP").getTestReason());
            startTest.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_START_APP").getTestReasonEn());
            stopTest.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_STOP_APP").getTestResult());
            stopTest.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_STOP_APP").getTestReason());
            stopTest.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_STOP_APP").getTestReasonEn());
            exceptionTest.setResult(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_RELIABLE_TEST").getTestResult());
            exceptionTest.setReason(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_RELIABLE_TEST").getTestReason());
            exceptionTest.setReasonEn(Constants.INFO_TEST_CASE_RESULT_MAP.get("NO_RELIABLE_TEST").getTestReasonEn());
        }
        Map<String, CompatibilityTestResult> resultMap = new HashMap<>();
        resultMap.put(Constants.CompatibilityTestName.APPLICATION_START.getTestName(), startTest);
        resultMap.put(Constants.CompatibilityTestName.APPLICATION_STOP.getTestName(), stopTest);
        resultMap.put(Constants.CompatibilityTestName.EXCEPTION_KILL.getTestName(), exceptionTest);
        return resultMap;
    }
}
