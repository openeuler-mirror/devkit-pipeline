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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ParseCpuPerFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseCpuPerFiles implements CompatibilityFilesParser {
    private static final String CPU_DESC = "CPU";

    private final ParsePerformanceFilesImpl parsePerformanceFiles;

    /**
     * construction function
     *
     * @param inputByteMap map
     */
    public ParseCpuPerFiles(Map<String, byte[]> inputByteMap) {
        this.parsePerformanceFiles = new ParsePerformanceFilesImpl(inputByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parsePerformanceFiles.getResult(Constants.CompatibilityTestName.PRESSURE_CPU.getTestName(),
                CPU_DESC, CPU_DESC, 2);
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.PRESSURE_CPU.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing pressure test CPU file ", ex);
            testResult.setResult(Constants.TEST_SKIPPED);
            testResult.setReason(String.format(Locale.ROOT,
                    Constants.PERFORMANCE_DESC_LIST.get(3), CPU_DESC));
            testResult.setReasonEn(String.format(Locale.ROOT,
                    Constants.PERFORMANCE_DESC_EN_LIST.get(3), CPU_DESC));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception in parsing pressure test CPU file ", ex);

            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT,
                    Constants.PERFORMANCE_DESC_LIST.get(2), CPU_DESC));
            testResult.setReasonEn(String.format(Locale.ROOT,
                    Constants.PERFORMANCE_DESC_EN_LIST.get(2), CPU_DESC));
        }
        return testResult;
    }

    private static class ParsePerformanceFilesImpl extends AbsParsePerformanceFiles {
        private final Pattern idlePattern = Pattern.compile(
                "\\d{2}(:|时)\\d{2}(:|分)\\d{2}(秒|\\s(PM|AM|HKT|))\\s+all\\s+");

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
                throw new BaseException("解析压力测试CPU文件缺失");
            }
            CompatibilityTestResult testResult = new CompatibilityTestResult(
                    Constants.CompatibilityTestName.PRESSURE_CPU.getTestName());
            List<String> idleList = getIdleList(fileBytes);
            if (idleList.size() > 0) {
                List<String> evidence = new ArrayList<>(idleList.subList(1, idleList.size()));
                List<String> evidenceEn = new ArrayList<>(idleList.subList(1, idleList.size()));
                if (Double.parseDouble(idleList.get(0)) <= 5.0d) {
                    testResult.setResult(Constants.TEST_PASSED);
                    evidence.add(String.format(Locale.ROOT,
                            Constants.PERFORMANCE_DESC_LIST.get(0), CPU_DESC,
                            Double.parseDouble(idleList.get(0)), "小于"));
                    evidenceEn.add(String.format(Locale.ROOT,
                            Constants.PERFORMANCE_DESC_EN_LIST.get(0), CPU_DESC,
                            Double.parseDouble(idleList.get(0)), "less"));
                } else {
                    testResult.setResult(Constants.TEST_FAILED);
                    evidence.add(String.format(Locale.ROOT,
                            Constants.PERFORMANCE_DESC_LIST.get(0), CPU_DESC,
                            Double.parseDouble(idleList.get(0)), "大于"));
                    evidenceEn.add(String.format(Locale.ROOT,
                            Constants.PERFORMANCE_DESC_EN_LIST.get(0), CPU_DESC,
                            Double.parseDouble(idleList.get(0)), "greater"));
                    testResult.setReason(String.format(Locale.ROOT,
                            Constants.PERFORMANCE_DESC_LIST.get(0), CPU_DESC,
                            Double.parseDouble(idleList.get(0)), "大于")
                            + Constants.PERFORMANCE_DESC_LIST.get(1));
                    testResult.setReasonEn(String.format(Locale.ROOT,
                            Constants.PERFORMANCE_DESC_EN_LIST.get(0),
                            CPU_DESC,
                            Double.parseDouble(idleList.get(0)), "greater")
                            + Constants.PERFORMANCE_DESC_EN_LIST.get(1));
                }
                testResult.setEvidence(evidence);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_LIST.get(2), CPU_DESC));
                testResult.setReasonEn(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_EN_LIST.get(2), CPU_DESC));
            }
            return testResult;
        }

        @Override
        List<String> getIdleList(byte[] fileBytes) throws IOException {
            Double maxIdle = 0.0d;
            String maxEvidence = null;
            Double minIdle = 100.0d;
            String minEvidence = null;
            try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
                 BufferedReader beginReader = new BufferedReader(
                        new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
                String line;
                while ((line = beginReader.readLine()) != null && idlePattern.matcher(line).find()) {
                    String idle = line.substring(line.length() - 8).trim();
                    if (maxIdle <= Double.parseDouble(idle)) {
                        maxIdle = Double.parseDouble(idle);
                        maxEvidence = line;
                    }
                    if (minIdle >= Double.parseDouble(idle)) {
                        minIdle = Double.parseDouble(idle);
                        minEvidence = line;
                    }
                }
            }

            List<String> idleList = new ArrayList<>();
            if (maxEvidence != null) {
                idleList.add(String.format(Locale.ROOT, "%.2f", new BigDecimal(maxIdle.toString())
                        .subtract(new BigDecimal(minIdle.toString())).abs().doubleValue()));
                idleList.add(maxEvidence);
                idleList.add(minEvidence);
            }
            return idleList;
        }

        @Override
        Map<String, Map<String, List<String>>> getIdleMap(byte[] fileBytes) {
            return new HashMap<>();
        }
    }
}
