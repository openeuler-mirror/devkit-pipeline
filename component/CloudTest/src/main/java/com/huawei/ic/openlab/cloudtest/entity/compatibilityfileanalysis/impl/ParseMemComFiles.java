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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ParseMemComFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseMemComFiles implements CompatibilityFilesParser {
    private static final String MEMORY_CN = "内存";
    private static final String MEMORY_EN = "memory";
    private static final Pattern PATTERN = Pattern.compile("\\d+\\.\\d+");

    private final ParseCompatibilityFilesImpl parseCompatibilityFiles;

    /**
     * ParseMemComFiles
     *
     * @param beginMemByteMap beginMemByteMap
     * @param endMemByteMap endMemByteMap
     */
    public ParseMemComFiles(Map<String, byte[]> beginMemByteMap, Map<String, byte[]> endMemByteMap) {
        this.parseCompatibilityFiles = new ParseCompatibilityFilesImpl(beginMemByteMap, endMemByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parseCompatibilityFiles.getResult(Constants.CompatibilityTestName.IDLE_MEM.getTestName(), MEMORY_CN,
                MEMORY_EN, 3);
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult =
                new CompatibilityTestResult(Constants.CompatibilityTestName.IDLE_MEM.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing idle test memory file ", ex.getMessage());
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(3), MEMORY_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(3), MEMORY_EN));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception occur in parsing idle test memory file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), MEMORY_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2), MEMORY_EN));
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
                throw new BaseException("解析空载测试内存文件缺失");
            }
            CompatibilityTestResult testResult =
                    new CompatibilityTestResult(Constants.CompatibilityTestName.IDLE_MEM.getTestName());
            List<String> beginAverageList = getAverage(beginBytes);
            List<String> endAverageList = getAverage(endBytes);
            double memMaxUsedIdle = 0.0d;
            List<String> evidence;
            List<String> evidenceEn;
            if (beginAverageList.size() > 0 && endAverageList.size() > 0) {
                memMaxUsedIdle = new BigDecimal(beginAverageList.get(0))
                        .subtract(new BigDecimal(endAverageList.get(0)))
                        .abs().doubleValue();
                if (memMaxUsedIdle <= 1.0) {
                    testResult.setResult(Constants.TEST_PASSED);
                    evidence = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1), MEMORY_CN,
                                    memMaxUsedIdle, "小于"));
                    evidenceEn = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1), MEMORY_EN,
                                    memMaxUsedIdle, "less"));
                } else {
                    testResult.setResult(Constants.TEST_FAILED);
                    evidence = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1), MEMORY_CN,
                                    memMaxUsedIdle, "大于"));
                    evidenceEn = Arrays.asList(beginAverageList.get(1), endAverageList.get(1),
                            String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1), MEMORY_EN,
                                    memMaxUsedIdle, "greater"));
                    testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(1),
                            MEMORY_CN, memMaxUsedIdle, "大于"));
                    testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(1),
                            MEMORY_EN, memMaxUsedIdle, "greater"));
                }
                testResult.setEvidence(evidence);
                testResult.setEvidenceEn(evidenceEn);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), MEMORY_CN));
                testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2),
                        MEMORY_EN));
            }
            return testResult;
        }

        @Override
        List<String> getAverage(byte[] fileBytes) {
            try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
                 BufferedReader br = new BufferedReader(
                        new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
                String line;
                List<String> averageList = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    average(line, averageList);
                }
                return averageList;
            } catch (IOException e) {
                throw new BaseException(e.getMessage());
            }
        }

        private void average(String line, List<String> averageList) {
            if (StringUtils.containsIgnoreCase(line, "平均时间")
                    || StringUtils.startsWithIgnoreCase(line,
                    "Average")) {
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.find()) {
                    averageList.add(matcher.group());
                    averageList.add(line.replace(System.lineSeparator(), ""));
                }
            }
        }
    }
}
