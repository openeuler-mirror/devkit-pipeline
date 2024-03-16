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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ParseNetPerFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseSoftwareLog implements CompatibilityFilesParser {
    private final byte[] configBytes;
    private final byte[] productNameBytes;

    /**
     * construction function
     *
     * @param configBytes config bytes
     * @param productNameBytes product name bytes
     */
    public ParseSoftwareLog(byte[] configBytes, byte[] productNameBytes) {
        this.configBytes = configBytes;
        this.productNameBytes = productNameBytes;
    }

    private Map<String, List<String>> parseConfig() {
        Map<String, List<String>> configMap = new HashMap<>();
        if (configBytes.length == 0) {
            return configMap;
        }

        String line;
        Pattern appPattern = Pattern.compile(Constants.APPLICATION_NAMES);
        Pattern startPattern = Pattern.compile(Constants.START_APP_COMMANDS);
        Pattern stopPattern = Pattern.compile(Constants.STOP_APP_COMMANDS);
        try (InputStream input = new ByteArrayInputStream(configBytes);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                Matcher appMatcher = appPattern.matcher(line);
                if (appMatcher.find()) {
                    List<String> applicationNamesList = Arrays.asList(appMatcher.group(1).split(","));
                    configMap.put("applicationNames", applicationNamesList);
                }

                Matcher startMatcher = startPattern.matcher(line);
                if (startMatcher.find()) {
                    List<String> startAppCommandsList = Arrays.asList(startMatcher.group(1).split(","));
                    configMap.put("startAppCommands", startAppCommandsList);
                }

                Matcher stopMatcher = stopPattern.matcher(line);
                if (stopMatcher.find()) {
                    List<String> stopAppCommandsList = Arrays.asList(stopMatcher.group(1).split(","));
                    configMap.put("stopAppCommands", stopAppCommandsList);
                }
            }
        } catch (IllegalStateException | IOException ex) {
            log.error("Error reading configuration file for compatibility testing tool", ex);
        }
        return configMap;
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SOFTWARE_NAME.getTestName());
        if (this.productNameBytes.length == 0) {
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SOFTWARE_COMPARE_DESC_LIST.get(3));
            return testResult;
        }
        Map<String, List<String>> configMap = parseConfig();
        if (configMap.size() == 0 || configMap.get("applicationNames").size() == 0) {
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SOFTWARE_COMPARE_DESC_LIST.get(2));
        } else {
            testResult.setApplicationNames(configMap.get("applicationNames"));
            testResult.setStartAppCommands(configMap.get("startAppCommands"));
            testResult.setStopAppCommands(configMap.get("stopAppCommands"));
        }

        getApplicationNames(configMap, testResult, this.productNameBytes);
        return testResult;
    }

    private void getApplicationNames(Map<String, List<String>> configMap, CompatibilityTestResult testResult,
                                     byte[] productNameBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(productNameBytes);
             BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            Map<String, Integer> appCountMap = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                getApplicationName(line, configMap, testResult, appCountMap);
            }
            for (Map.Entry<String, Integer> entry : appCountMap.entrySet()) {
                if (appCountMap.get(entry.getKey()) == 0) {
                    testResult.setResult(Constants.TEST_FAILED);
                    List<String> evidenceList = testResult.getEvidence();
                    evidenceList.add(String.format(Locale.ROOT,
                            Constants.SOFTWARE_COMPARE_DESC_LIST.get(0), entry.getKey()));
                    testResult.setEvidence(evidenceList);
                    evidenceList = testResult.getEvidenceEn();
                    evidenceList.add(String.format(Locale.ROOT,
                            Constants.SOFTWARE_COMPARE_DESC_EN_LIST.get(0), entry.getKey()));
                    testResult.setEvidenceEn(evidenceList);
                }
            }
            if (Constants.TEST_FAILED.equals(testResult.getResult())) {
                testResult.setReason(String.format(Locale.ROOT,
                        Constants.SOFTWARE_COMPARE_DESC_LIST.get(1), configMap.get("applicationNames").toString()));
                testResult.setReasonEn(String.format(Locale.ROOT,
                        Constants.SOFTWARE_COMPARE_DESC_EN_LIST.get(1), configMap.get("applicationNames").toString()));
            } else {
                testResult.setResult(Constants.TEST_PASSED);
            }
        } catch (IOException e) {
            throw new BaseException(e.getMessage());
        }
    }

    private void getApplicationName(String line, Map<String, List<String>> configMap,
                                    CompatibilityTestResult testResult, Map<String, Integer> appCountMap) {
        int userLength = line.split(" ")[0].length();
        String lineSplit = line.substring(userLength);

        for (String app : configMap.get("applicationNames")) {
            if (StringUtils.startsWithIgnoreCase(lineSplit, app)) {
                appCountMap.put(app, appCountMap.getOrDefault(app, 0) + 1);
                List<String> evidenceList = testResult.getEvidence() == null ? new ArrayList<>()
                        : testResult.getEvidence();
                evidenceList.add(line.replace(System.lineSeparator(), ""));
                testResult.setEvidence(evidenceList);
                testResult.setEvidenceEn(evidenceList);
            }
        }
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.SOFTWARE_NAME.getTestName());
        try {
            testResult = getResult();
        } catch (IllegalStateException | IOException ex) {
            log.error("Error parsing configuration file and software process stack information of compatibility "
                    + "testing tool", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SOFTWARE_COMPARE_DESC_LIST.get(4));
            testResult.setReasonEn(Constants.SOFTWARE_COMPARE_DESC_EN_LIST.get(4));
        }
        return testResult;
    }
}
