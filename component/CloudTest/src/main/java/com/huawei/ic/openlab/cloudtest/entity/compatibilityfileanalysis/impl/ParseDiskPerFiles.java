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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ParseDiskPerFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseDiskPerFiles implements CompatibilityFilesParser {
    private static final String DRIVE_CN = "硬盘";
    private static final String DRIVE_EN = "drive";

    private final ParsePerformanceFilesImpl parsePerformanceFiles;

    /**
     * construction function
     *
     * @param inputByteMap MAP
     */
    public ParseDiskPerFiles(Map<String, byte[]> inputByteMap) {
        this.parsePerformanceFiles = new ParsePerformanceFilesImpl(inputByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parsePerformanceFiles.getResult(Constants.CompatibilityTestName.PRESSURE_DISK.getTestName(),
                DRIVE_CN, DRIVE_EN, 2);
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.PRESSURE_DISK.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing pressure test disk file ", ex.getMessage());
            testResult.setResult(Constants.TEST_SKIPPED);
            testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(3), DRIVE_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(3), DRIVE_EN));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception occur in parsing pressure test disk file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(2), DRIVE_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(2), DRIVE_EN));
        }
        return testResult;
    }

    private static class ParsePerformanceFilesImpl extends AbsParsePerformanceFiles {
        private final Pattern idlePattern = Pattern.compile(
                "\\d{2}(:|时)\\d{2}(:|分)\\d{2}(秒|\\s(PM|AM|HKT|))\\s+");

        /**
         * ParsePerformanceFilesImpl
         *
         * @param inputByteMap inputByteMap
         */
        public ParsePerformanceFilesImpl(Map<String, byte[]> inputByteMap) {
            super(inputByteMap);
        }

        @Override
        CompatibilityTestResult readFile(byte[] fileBytes) throws IOException {
            if (fileBytes.length == 0) {
                throw new BaseException("解析压力测试硬盘文件缺失");
            }
            CompatibilityTestResult testResult = new CompatibilityTestResult(
                    Constants.CompatibilityTestName.PRESSURE_DISK.getTestName());
            Map<String, Map<String, List<String>>> idleMap = getIdleMap(fileBytes);
            if (idleMap.size() > 0) {
                Double maxGap = 0.00d;
                String maxDiskName = null;
                String diskMax = null;
                String diskMin = null;
                for (Map.Entry<String, Map<String, List<String>>> entry : idleMap.entrySet()) {
                    List<String> idleList = entry.getValue().get("idle");
                    double diskMaxGap = idleList.stream().mapToDouble(Double::parseDouble).max().orElse(0.00d);
                    double diskMinGap = idleList.stream().mapToDouble(Double::parseDouble).min().orElse(0.00d);
                    Double gap = new BigDecimal(String.valueOf(diskMaxGap))
                            .subtract(new BigDecimal(String.valueOf(diskMinGap))).abs().doubleValue();
                    if (maxGap <= gap) {
                        maxGap = gap;
                        maxDiskName = entry.getKey();
                        diskMax = String.format(Locale.ROOT, "%.2f", diskMaxGap);
                        diskMin = String.format(Locale.ROOT, "%.2f", diskMinGap);
                    }
                }
                testResult = getTestResult(maxDiskName, maxGap, idleMap, diskMax, diskMin);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_LIST.get(2), DRIVE_CN));
                testResult.setReasonEn(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_EN_LIST.get(2), DRIVE_EN));
            }
            return testResult;
        }

        private CompatibilityTestResult getTestResult(String maxDiskName, Double maxGap,
                                                      Map<String, Map<String, List<String>>> idleMap,
                                                      String diskMax, String diskMin) {
            CompatibilityTestResult testResult = new CompatibilityTestResult(
                    Constants.CompatibilityTestName.PRESSURE_DISK.getTestName());
            List<String> evidenceList = idleMap.get(maxDiskName).get("evidence");
            List<String> evidence = new ArrayList<>();
            evidence.add(evidenceList.stream().filter(
                            s -> StringUtils.containsIgnoreCase(s, diskMax)).limit(1)
                    .collect(Collectors.joining()));
            evidence.add(evidenceList.stream().filter(
                            s -> StringUtils.containsIgnoreCase(s, diskMin)).limit(1)
                    .collect(Collectors.joining()));
            List<String> evidenceEn = new ArrayList<>(evidence);
            if (maxGap <= 5.0) {
                testResult.setResult(Constants.TEST_PASSED);
                evidence.add(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_LIST.get(0), DRIVE_CN, maxGap,
                        "小于"));
                evidenceEn.add(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_EN_LIST.get(0), DRIVE_EN, maxGap, "less"));
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                evidence.add(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(0),
                        DRIVE_CN, maxGap, "大于"));
                evidenceEn.add(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(0),
                        DRIVE_EN, maxGap, "greater"));
                testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(0),
                        DRIVE_CN, maxGap, "大于"));
                testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(0),
                        DRIVE_EN, maxGap, "greater"));
            }
            testResult.setEvidence(evidence);
            testResult.setEvidenceEn(evidenceEn);
            return testResult;
        }

        @Override
        List<String> getIdleList(byte[] fileBytes) {
            return new ArrayList<>();
        }

        @Override
        Map<String, Map<String, List<String>>> getIdleMap(byte[] fileBytes) throws IOException {
            String line = null;
            Map<String, Map<String, List<String>>> averageMap = new HashMap<>();
            Map<String, Integer> indexMap = new HashMap<>();
            try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
                 BufferedReader beginReader = new BufferedReader(new InputStreamReader(beginInput,
                        StandardCharsets.UTF_8))) {
                boolean flag = true;

                while ((line = beginReader.readLine()) != null) {
                    if (!StringUtils.containsIgnoreCase(line, "DEV") && idlePattern.matcher(line).find()) {
                        String[] split = line.split("\\s+");

                        String idle = split[indexMap.get("%util")];

                        String diskName = split[indexMap.get("DEV")];
                        Map<String, List<String>> diskMap = averageMap.computeIfAbsent(diskName, e -> new HashMap<>());
                        diskMap.computeIfAbsent("idle", e -> new ArrayList<>()).add(idle);
                        diskMap.computeIfAbsent("evidence", e -> new ArrayList<>()).add(line);
                    } else if (flag && StringUtils.containsIgnoreCase(line, "DEV")) {
                        indexMap = getIndexMap(line);
                        flag = false;
                    }
                }
            }
            return averageMap;
        }

        private Map<String, Integer> getIndexMap(String line) {
            Map<String, Integer> indexMap = new HashMap<>();
            String[] split = line.split("\\s+");
            for (int i =0; i < split.length; i++) {
                if (split[i].equals("DEV")) {
                    indexMap.put("DEV", i);
                } else if (split[i].equals("%util")) {
                    indexMap.put("%util", i);
                }
            }
            return indexMap;
        }
    }
}
