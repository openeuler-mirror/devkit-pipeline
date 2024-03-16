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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ParseNetComFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseNetComFiles implements CompatibilityFilesParser {
    private static final String NIC_CN = "网卡";
    private static final String NIC_EN = "NIC";

    private final ParseCompatibilityFilesImpl parseCompatibilityFiles;

    /**
     * construction function
     *
     * @param beginNetByteMap beginNetByteMap
     * @param endNetByteMap endNetByteMap
     */
    public ParseNetComFiles(Map<String, byte[]> beginNetByteMap, Map<String, byte[]> endNetByteMap) {
        this.parseCompatibilityFiles = new ParseCompatibilityFilesImpl(beginNetByteMap, endNetByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parseCompatibilityFiles.getResult(Constants.CompatibilityTestName.IDLE_NET.getTestName(),
                NIC_CN, NIC_EN, 2);
    }

    @Override
    public CompatibilityTestResult parseFiles() {
        CompatibilityTestResult testResult = new CompatibilityTestResult(
                Constants.CompatibilityTestName.IDLE_NET.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing idle test net file ", ex.getMessage());
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(3), NIC_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(3), NIC_EN));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception in parsing idle test net file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(2), NIC_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(2), NIC_EN));
        }
        return testResult;
    }

    private static class ParseCompatibilityFilesImpl extends AbsParseCompatibilityFiles {
        private final Pattern averagePattern = Pattern.compile("(平均时间|Average):\\s*(?!IFACE|lo)\\w.*");
        private final Pattern pattern = Pattern.compile("\\d+\\.\\d+");

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
                throw new BaseException("解析空载测试网卡文件缺失");
            }
            CompatibilityTestResult testResult = new CompatibilityTestResult(
                    Constants.CompatibilityTestName.IDLE_NET.getTestName());
            Map<String, List<String>> beginAverageMap = getAverageMap(beginBytes);
            Map<String, List<String>> endAverageMap = getAverageMap(endBytes);

            if (beginAverageMap.size() > 0 && endAverageMap.size() > 0) {
                createTestResult(testResult, beginAverageMap, endAverageMap);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_LIST.get(2), NIC_CN));
                testResult.setReasonEn(String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_EN_LIST.get(2), NIC_EN));
            }
            return testResult;
        }

        private void createTestResult(CompatibilityTestResult testResult, Map<String, List<String>> beginAverageMap,
                                      Map<String, List<String>> endAverageMap) {
            Double rxkbIdle;
            Double txkbIdle;
            Double maxRxkbIdle = 0.0d;
            Double maxTxkbIdle = 0.0d;
            String rxkbNetName = null;
            String txkbNetName = null;
            BigDecimal num = new BigDecimal(8).divide(new BigDecimal(1000 * 1024));
            for (Map.Entry<String, List<String>> entry : beginAverageMap.entrySet()) {
                if (!endAverageMap.containsKey(entry.getKey())) {
                    continue;
                }
                rxkbIdle = new BigDecimal(beginAverageMap.get(entry.getKey()).get(0))
                        .subtract(new BigDecimal(endAverageMap.get(entry.getKey()).get(0)))
                        .abs().multiply(num).doubleValue();
                txkbIdle = new BigDecimal(beginAverageMap.get(entry.getKey()).get(1))
                        .subtract(new BigDecimal(endAverageMap.get(entry.getKey()).get(1)))
                        .abs().multiply(num).doubleValue();
                if (rxkbIdle >= maxRxkbIdle) {
                    maxRxkbIdle = rxkbIdle;
                    rxkbNetName = entry.getKey();
                }
                if (txkbIdle >= maxTxkbIdle) {
                    maxTxkbIdle = txkbIdle;
                    txkbNetName = entry.getKey();
                }
            }
            List<String> evidence = new ArrayList<>();
            evidence.add(beginAverageMap.get(rxkbNetName).get(2));
            evidence.add(endAverageMap.get(rxkbNetName).get(2));
            List<String> evidenceEn = new ArrayList<>(evidence);

            int[] kbpass = {0, 0};
            checkMaxRxkbIdle(maxRxkbIdle, kbpass, evidence, evidenceEn);
            evidence.add(beginAverageMap.get(txkbNetName).get(2));
            evidence.add(endAverageMap.get(txkbNetName).get(2));
            evidenceEn.add(beginAverageMap.get(txkbNetName).get(2));
            evidenceEn.add(endAverageMap.get(txkbNetName).get(2));
            checkMaxTxkbIdle(maxTxkbIdle, kbpass, evidence, evidenceEn);

            testResult.setEvidence(evidence);
            testResult.setEvidenceEn(evidenceEn);
            setTestResult(maxRxkbIdle, maxTxkbIdle, kbpass, testResult);
        }

        private void checkMaxRxkbIdle(Double maxRxkbIdle, int[] kbpass, List<String> evidence,
                                      List<String> evidenceEn) {
            Double maxRxkb = new BigDecimal(maxRxkbIdle.toString()).multiply(new BigDecimal(100)).doubleValue();
            if (maxRxkbIdle <= 0.01) {
                kbpass[0] = 1;
                evidence.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(6),
                        "接收", maxRxkb, "小于"));
                evidenceEn.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(6),
                        "received", maxRxkb, "less"));
            } else {
                evidence.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(6),
                        "接收", maxRxkb, "大于"));
                evidenceEn.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(6),
                        "received", maxRxkb, "greater"));
            }
        }

        private void checkMaxTxkbIdle(Double maxTxkbIdle, int[] kbpass, List<String> evidence,
                                      List<String> evidenceEn) {
            Double maxTxkb = new BigDecimal(maxTxkbIdle.toString()).multiply(new BigDecimal(100)).doubleValue();
            if (maxTxkbIdle < 0.01) {
                kbpass[1] = 1;
                evidence.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(6), "发送",
                        maxTxkb, "小于"));
                evidenceEn.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(6), "sent",
                        maxTxkb, "less"));
            } else {
                evidence.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(6), "发送",
                        maxTxkb, "大于"));
                evidenceEn.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(6), "sent",
                        maxTxkb, "greater"));
            }
        }

        private void setTestResult(Double maxRxkbIdle, Double maxTxkbIdle, int[] kbpass,
                                   CompatibilityTestResult testResult) {
            if (kbpass[0] == 1 && kbpass[1] == 1) {
                testResult.setResult(Constants.TEST_PASSED);
            } else {
                double maxRxkb = new BigDecimal(maxRxkbIdle.toString()).multiply(new BigDecimal(100)).doubleValue();
                double maxTxkb = new BigDecimal(maxTxkbIdle.toString()).multiply(new BigDecimal(100)).doubleValue();
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_LIST.get(7), "发送",
                        maxTxkb) + String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_LIST.get(7), "接收",
                        maxRxkb));
                testResult.setReasonEn(String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_EN_LIST.get(7),
                        "sent", maxTxkb) + String.format(Locale.ROOT,
                        Constants.COMPATIBILITY_DESC_LIST.get(7), "received",
                        maxRxkb));
            }
        }

        @Override
        List<String> getAverage(byte[] fileBytes) {
            return new ArrayList<>();
        }

        /**
         * get Average Map
         *
         * @param fileBytes fileBytes
         * @return Map
         * @throws IOException IOException
         */
        public Map<String, List<String>> getAverageMap(byte[] fileBytes) throws IOException {
            Map<String, List<String>> averageMap = new HashMap<>();
            try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
                 BufferedReader beginReader = new BufferedReader(
                        new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
                String line;

                while ((line = beginReader.readLine()) != null) {
                    if (!averagePattern.matcher(line).find()) {
                        continue;
                    }
                    Matcher matcher = pattern.matcher(line);
                    String netName = line.substring(13).trim().split(" ")[0];
                    List<String> matchList = new ArrayList<>();
                    while (matcher.find()) {
                        matchList.add(matcher.group().trim());
                    }
                    averageMap.put(netName, Arrays.asList(matchList.get(2), matchList.get(3), line));
                }
            }
            return averageMap;
        }
    }
}
