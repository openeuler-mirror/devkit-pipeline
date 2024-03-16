/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityFilesParser;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.util.Constants;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
/**
 * ParseDiskComFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseDiskComFiles implements CompatibilityFilesParser {
    private static final String DRIVE_CN = "硬盘";
    private static final String DRIVE_EN = "drive";

    private final ParseCompatibilityFilesImpl parseCompatibilityFiles;

    /**
     * ParseDiskComFiles
     *
     * @param beginDiskByteMap beginDiskByteMap
     * @param endDiskByteMap endDiskByteMap
     */
    public ParseDiskComFiles(Map<String, byte[]> beginDiskByteMap, Map<String, byte[]> endDiskByteMap) {
        this.parseCompatibilityFiles = new ParseCompatibilityFilesImpl(beginDiskByteMap, endDiskByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parseCompatibilityFiles.getResult(Constants.CompatibilityTestName.IDLE_DISK.getTestName(),
                DRIVE_CN, DRIVE_EN, 2);
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.IDLE_DISK.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing idle test disk file ", ex.getMessage());
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(3), DRIVE_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(3), DRIVE_EN));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception in parsing idle test disk file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), DRIVE_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2), DRIVE_EN));
        }
        return testResult;
    }

    private static class ParseCompatibilityFilesImpl extends AbsParseCompatibilityFiles {
        private final Pattern averagePattern = Pattern.compile("(平均时间|Average):");

        /**
         * ParseCompatibilityFilesImpl
         *
         * @param beginByteMap beginByteMap
         * @param endByteMap endByteMap
         */
        protected ParseCompatibilityFilesImpl(Map<String, byte[]> beginByteMap, Map<String, byte[]> endByteMap) {
            super(beginByteMap, endByteMap);
        }

        @Override
        CompatibilityTestResult readFile(byte[] beginBytes, byte[] endBytes) throws IOException {
            if (beginBytes.length == 0 || endBytes.length == 0) {
                throw new BaseException("解析空载测试硬盘文件缺失");
            }

            Map<String, List<String>> beginAverageMap = getAverageMap(beginBytes);
            Map<String, List<String>> endAverageMap = getAverageMap(endBytes);

            double diskMaxUsedIdle = 0.0d;
            String maxDiskName = null;
            double diskIdle = 0.0d;
            if (beginAverageMap.size() > 0 && endAverageMap.size() > 0) {
                for (Map.Entry<String, List<String>> entry : beginAverageMap.entrySet()) {
                    if (!endAverageMap.containsKey(entry.getKey())) {
                        continue;
                    }
                    diskIdle = new BigDecimal(beginAverageMap.get(entry.getKey()).get(0))
                            .subtract(new BigDecimal(endAverageMap.get(entry.getKey()).get(0)))
                            .abs().doubleValue();
                    if (diskMaxUsedIdle <= diskIdle) {
                        maxDiskName = entry.getKey();
                        diskMaxUsedIdle = diskIdle;
                    }
                }
            }
            return getTestResult(maxDiskName, diskMaxUsedIdle, beginAverageMap, endAverageMap);
        }

        private CompatibilityTestResult getTestResult(String maxDiskName, double diskMaxUsedIdle,
                                                      Map<String, List<String>> beginAverageMap, Map<String,
                List<String>> endAverageMap) {
            CompatibilityTestResult testResult = new CompatibilityTestResult(
                    Constants.CompatibilityTestName.IDLE_DISK.getTestName());
            List<String> evidence;
            List<String> evidenceEn;
            if (maxDiskName != null) {
                if (diskMaxUsedIdle <= 1.0) {
                    testResult.setResult(Constants.TEST_PASSED);
                    evidence = Arrays.asList(beginAverageMap.get(maxDiskName).get(1),
                            endAverageMap.get(maxDiskName).get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1), DRIVE_CN,
                                    diskMaxUsedIdle, "小于"));
                    evidenceEn = Arrays.asList(beginAverageMap.get(maxDiskName).get(1),
                            endAverageMap.get(maxDiskName).get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1),
                                    DRIVE_EN, diskMaxUsedIdle, "less"));
                } else {
                    testResult.setResult(Constants.TEST_FAILED);
                    evidence = Arrays.asList(beginAverageMap.get(maxDiskName).get(1),
                            endAverageMap.get(maxDiskName).get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1),
                                    DRIVE_CN, diskMaxUsedIdle, "大于"));
                    evidenceEn = Arrays.asList(beginAverageMap.get(maxDiskName).get(1),
                            endAverageMap.get(maxDiskName).get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1),
                                    DRIVE_EN, diskMaxUsedIdle, "greater"));
                }
                testResult.setEvidence(evidence);
                testResult.setEvidenceEn(evidenceEn);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), DRIVE_CN));
                testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2),
                        DRIVE_EN));
            }
            return testResult;
        }

        @Override
        List<String> getAverage(byte[] fileBytes) {
            return new ArrayList<>();
        }

        /**
         * Get average map
         *
         * @param cpuBytes cpu bytes
         * @return average map
         * @throws IOException exception
         */
        public Map<String, List<String>> getAverageMap(byte[] cpuBytes) throws IOException {
            String line = null;
            Map<String, List<String>> averageMap = new HashMap<>();
            Map<String, Integer> indexMap = new HashMap<>();
            try (InputStream beginInput = new ByteArrayInputStream(cpuBytes);
                 BufferedReader beginReader = new BufferedReader(
                        new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
                boolean flag = true;
                while ((line = beginReader.readLine()) != null) {
                    if (!StringUtils.containsIgnoreCase(line, "DEV") && averagePattern.matcher(line).find()) {
                        String[] split = line.split("\\s+");
                        String idle = split[indexMap.get("%util")];
                        String diskName = split[indexMap.get("DEV")];
                        List<String> averageList = new ArrayList<>();
                        averageList.add(idle);
                        averageList.add(line.replace("\n", ""));
                        averageMap.put(diskName, averageList);
                    } else if (flag && StringUtils.containsIgnoreCase(line, "DEV")
                                && averagePattern.matcher(line).find() ){
                        indexMap = getIndexMap(line);
                        flag = false;
                    }
                }
            }
            return averageMap;
        }

        private static Map<String, Integer> getIndexMap(String line) {
            Map<String, Integer> indexMap = new HashMap<>();
            String[] split =line.split("\\s+");
            for (int i = 0; i < split.length; i++) {
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
