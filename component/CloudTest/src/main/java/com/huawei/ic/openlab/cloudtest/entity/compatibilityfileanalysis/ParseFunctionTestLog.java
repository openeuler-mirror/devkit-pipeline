/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.util.Constants;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ParseFunctionTestLog
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseFunctionTestLog {
    private static final Pattern RUN_PATTERN = Pattern.compile(Constants.PYTEST_RUN_PATTERN);
    private static final Pattern SUCCESS_PATTERN = Pattern.compile(Constants.PYTEST_SUCCESS_PATTERN);
    private static final Pattern FAIL_PATTERN = Pattern.compile(Constants.PYTEST_FAILED_PATTERN);
    private static final Pattern SHELL_TEST_PATTERN = Pattern.compile(Constants.SHELL_UNIT_TEST_PATTERN);
    private static final Pattern SHELL_RUN_PATTERN = Pattern.compile(Constants.SHELL_UNIT_RUN_PATTERN);
    private static final Pattern SHELL_FAIL_PATTERN = Pattern.compile(Constants.SHELL_UNIT_FAILED_PATTERN);

    private final byte[] shellBytes;
    private final byte[] pytestBytes;

    /**
     * constructin function
     *
     * @param shellBytes shellBytes
     * @param pytestBytes pytestBytes
     */
    public ParseFunctionTestLog(byte[] shellBytes, byte[] pytestBytes) {
        this.shellBytes = shellBytes;
        this.pytestBytes = pytestBytes;
    }

    /**
     * get Shell Result
     *
     * @return CompatibilityTestResult
     * @throws IOException IOException
     */
    public CompatibilityTestResult getShellResult() {
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put(Constants.TEST_FAILED, 0);
        resultMap.put(Constants.TEST_PASSED, 0);

        List<String> evidenceList = matches(resultMap);
        CompatibilityTestResult testResult = new CompatibilityTestResult("Function_Test");
        testResult.setEvidence(evidenceList);
        testResult.setReason(JSONObject.toJSONString(resultMap));
        return testResult;
    }

    private List<String> matches(Map<String, Integer> resultMap) {
        List<String> evidenceList = new ArrayList<>();
        try (InputStream inputStream = new ByteArrayInputStream(shellBytes);
             BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int totalTest = 0;
            while ((line = reader.readLine()) != null) {
                match(line, evidenceList, resultMap, totalTest);
            }
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        }
        return evidenceList;
    }

    private void match(String line, List<String> evidenceList, Map<String, Integer> resultMap,
                       int totalTest) {
        int failTest;
        int total = totalTest;
        Matcher testMatch = SHELL_TEST_PATTERN.matcher(line);
        if (testMatch.find()) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
        }
        Matcher matcher = SHELL_RUN_PATTERN.matcher(line);
        if (matcher.find()) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
            try {
                total = Integer.parseInt(matcher.group(1).trim());
            } catch (NumberFormatException ex) {
                total = 0;
            }
        }
        if (line.matches("^\\\u001B\\[1;32mOK\\\u001B\\[0m")) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
            resultMap.put(Constants.TEST_PASSED, total + resultMap.get(Constants.TEST_PASSED));
            total = 0;
        }
        Matcher failedMatcher = SHELL_FAIL_PATTERN.matcher(line);
        if (failedMatcher.find()) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
            try {
                failTest = Integer.parseInt(failedMatcher.group(1).trim());
            } catch (NumberFormatException ex) {
                failTest = 0;
            }
            if (failTest > total) {
                failTest = total;
            }
            resultMap.put(Constants.TEST_FAILED, failTest + resultMap.get(Constants.TEST_FAILED));
            if (total != 0) {
                resultMap.put(Constants.TEST_PASSED,
                        total - failTest + resultMap.get(Constants.TEST_PASSED));
            }
        }
    }

    private CompatibilityTestResult getPytestResult() {
        List<String> evidenceList = new ArrayList<>();
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put(Constants.TEST_FAILED, 0);
        resultMap.put(Constants.TEST_PASSED, 0);

        try (InputStream inputStream = new ByteArrayInputStream(pytestBytes);
             BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                matcher(line, evidenceList, resultMap);
            }
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        }
        CompatibilityTestResult testResult = new CompatibilityTestResult("Function_Test");
        testResult.setReason(JSONObject.toJSONString(resultMap));
        testResult.setEvidence(evidenceList);
        return testResult;
    }

    private void matcher(String line, List<String> evidenceList, Map<String, Integer> resultMap) {
        Matcher runMatcher = RUN_PATTERN.matcher(line);
        if (runMatcher.find()) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
        }
        Matcher matcher = SUCCESS_PATTERN.matcher(line);
        if (matcher.find()) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
            try {
                resultMap.put(Constants.TEST_PASSED,
                        Integer.parseInt(matcher.group(1).trim()) + resultMap.get(Constants.TEST_PASSED));
            } catch (NumberFormatException ex) {
                log.error("Error in parsing Python test results {}", line);
            }
        }
        Matcher failedMatcher = FAIL_PATTERN.matcher(line);
        if (failedMatcher.find()) {
            evidenceList.add(line.replace(System.lineSeparator(), ""));
            try {
                resultMap.put(Constants.TEST_PASSED,
                        Integer.parseInt(failedMatcher.group(2).trim())
                                + resultMap.get(Constants.TEST_PASSED));
                resultMap.put(Constants.TEST_FAILED,
                        Integer.parseInt(failedMatcher.group(1).trim())
                                + resultMap.get(Constants.TEST_FAILED));
            } catch (NumberFormatException ex) {
                log.error("Error in parsing Python test results {}", line);
            }
        }
    }

    /**
     * parseFunctionTestLog
     *
     * @return CompatibilityTestResult
     */
    public CompatibilityTestResult parseFunctionTestLog() {
        Map<String, Integer> resultMap = new HashMap<>();
        List<String> evidenceList = new ArrayList<>();
        CompatibilityTestResult testResult = new CompatibilityTestResult("Function_Test");
        try {
            if (shellBytes.length != 0) {
                CompatibilityTestResult shellResult = getShellResult();
                if (shellResult.getEvidence() != null && shellResult.getEvidence().size() > 0) {
                    evidenceList.addAll(shellResult.getEvidence());
                }
                if (shellResult.getReason() != null) {
                    JSONObject jsonObject = JSONObject.parseObject(shellResult.getReason());
                    resultMap.put(Constants.TEST_FAILED, jsonObject.getIntValue(Constants.TEST_FAILED));
                    resultMap.put(Constants.TEST_PASSED, jsonObject.getIntValue(Constants.TEST_PASSED));
                }
            }
            if (pytestBytes.length != 0) {
                CompatibilityTestResult pytestResult = getPytestResult();
                if (pytestResult.getEvidence() != null && pytestResult.getEvidence().size() > 0) {
                    evidenceList.addAll(pytestResult.getEvidence());
                }
                if (pytestResult.getReason() != null) {
                    JSONObject jsonObject = JSONObject.parseObject(pytestResult.getReason());
                    resultMap.put(Constants.TEST_FAILED, resultMap.getOrDefault(Constants.TEST_FAILED, 0)
                            + jsonObject.getIntValue(Constants.TEST_FAILED));
                    resultMap.put(Constants.TEST_PASSED, resultMap.getOrDefault(Constants.TEST_PASSED, 0)
                            + jsonObject.getIntValue(Constants.TEST_PASSED));
                }
            }
            testResult.setResult(Constants.TEST_PASSED);
            testResult.setReason(JSONObject.toJSONString(resultMap));
            testResult.setEvidence(evidenceList);
            if (pytestBytes.length == 0 & shellBytes.length == 0) {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason("没有功能测试文件");
            }
        } catch (BaseException e) {
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason("功能测试文件解析出错");
        }
        return testResult;
    }
}
