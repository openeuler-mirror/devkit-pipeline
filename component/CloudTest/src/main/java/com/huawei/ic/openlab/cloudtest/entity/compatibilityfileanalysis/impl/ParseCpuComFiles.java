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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ParseCpuComFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseCpuComFiles implements CompatibilityFilesParser {
    private static final String CPU_DESC = "CPU";

    private final ParseCompatibilityFilesImpl parseCompatibilityFiles;

    /**
     * ParseCpuComFiles
     *
     * @param beginCpuByteMap beginCpuByteMap
     * @param endCpuByteMap endCpuByteMap
     */
    public ParseCpuComFiles(Map<String, byte[]> beginCpuByteMap, Map<String, byte[]> endCpuByteMap) {
        this.parseCompatibilityFiles = new ParseCompatibilityFilesImpl(beginCpuByteMap, endCpuByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parseCompatibilityFiles.getResult(Constants.CompatibilityTestName.IDLE_CPU.getTestName(),
                CPU_DESC, CPU_DESC, 2);
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.IDLE_CPU.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing idle test cpu file ", ex.getMessage());
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(3), CPU_DESC));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(3), CPU_DESC));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception in parsing idle test cpu file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), CPU_DESC));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2), CPU_DESC));
        }
        return testResult;
    }

    private static class ParseCompatibilityFilesImpl extends AbsParseCompatibilityFiles {
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
                throw new BaseException("解析空载测试CPU文件缺失");
            }
            CompatibilityTestResult testResult = new CompatibilityTestResult(
                    Constants.CompatibilityTestName.IDLE_CPU.getTestName());
            List<String> beginAverageList = getAverage(beginBytes);
            List<String> endAverageList = getAverage(endBytes);
            double cpuMaxUsedIdle;
            if (beginAverageList.size() > 0 && endAverageList.size() > 0) {
                cpuMaxUsedIdle =
                        new BigDecimal(beginAverageList.get(0))
                                .subtract(new BigDecimal(endAverageList.get(0))).abs().doubleValue();
                if (cpuMaxUsedIdle <= 1.0) {
                    testResult.setResult(Constants.TEST_PASSED);
                    List<String> evidence = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1), CPU_DESC,
                                    cpuMaxUsedIdle, "小于"));
                    List<String> evidenceEn = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1),
                                    CPU_DESC, cpuMaxUsedIdle, "less"));
                    testResult.setEvidence(evidence);
                    testResult.setEvidenceEn(evidenceEn);
                } else {
                    testResult.setResult(Constants.TEST_FAILED);
                    List<String> evidence = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1), CPU_DESC,
                                    cpuMaxUsedIdle, "大于"));
                    testResult.setEvidence(evidence);
                    testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1),
                            CPU_DESC, cpuMaxUsedIdle, "大于"));
                    List<String> evidenceEn = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1),
                                    CPU_DESC, cpuMaxUsedIdle, "greater"));
                    testResult.setEvidenceEn(evidenceEn);
                    testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1),
                            CPU_DESC, cpuMaxUsedIdle, "greater"));
                }
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), CPU_DESC));
                testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2),
                        CPU_DESC));
            }
            return testResult;
        }

        @Override
        List<String> getAverage(byte[] fileBytes) throws IOException {
            List<String> averageList = new ArrayList<>();
            try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
                 BufferedReader beginReader = new BufferedReader(
                        new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
                String line;

                while ((line = beginReader.readLine()) != null) {
                    if (StringUtils.containsIgnoreCase(line, "平均时间")
                            || StringUtils.startsWithIgnoreCase(line, "Average")) {
                        averageList.add(line.substring(line.length() - 8).trim());
                        averageList.add(line.replace(System.lineSeparator(), ""));
                    }
                }
            }
            return averageList;
        }
    }
}
