/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityFilesParser;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.util.Constants;

import lombok.Data;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ParseNetPerFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseNetPerFiles implements CompatibilityFilesParser {
    private static final String NIC_CN = "网卡";
    private static final String NIC_EN = "NIC";

    private final ParsePerformanceFilesImpl parsePerformanceFiles;

    /**
     * construction function
     *
     * @param inputByteMap map
     */
    public ParseNetPerFiles(Map<String, byte[]> inputByteMap) {
        this.parsePerformanceFiles = new ParsePerformanceFilesImpl(inputByteMap);
    }

    @Override
    public CompatibilityTestResult getResult() throws IOException {
        return parsePerformanceFiles.getResult(Constants.CompatibilityTestName.PRESSURE_NET.getTestName(), NIC_CN,
                NIC_EN, 2);
    }

    @Override
    public CompatibilityTestResult parseFiles() throws IOException {
        CompatibilityTestResult testResult =
                new CompatibilityTestResult(Constants.CompatibilityTestName.PRESSURE_NET.getTestName());
        try {
            testResult = getResult();
        } catch (BaseException ex) {
            log.error("Missing pressure test net file ", ex.getMessage());
            testResult.setResult(Constants.TEST_SKIPPED);
            testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(3), NIC_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(3), NIC_EN));
        } catch (NumberFormatException | IOException ex) {
            log.error("Exception in parsing idle test net file ", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(2), NIC_CN));
            testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(2), NIC_EN));
        }
        return testResult;
    }

    @Data
    private static class XxkbRate {
        double maxXxkbRate = 0.00d;
        String maxXxkbDiskName = null;
        String netXxkbMax = null;
        String netXxkbMin = null;
        double netXxkbRate = 0.0d;
    }

    private static class ParsePerformanceFilesImpl extends AbsParsePerformanceFiles {
        private final Pattern idlePattern = Pattern.compile("\\d{2}(:|时)\\d{2}(:|分)\\d{2}(秒|\\s(PM|AM|HKT|))\\s+");
        private final Pattern pattern = Pattern.compile("\\d+\\.\\d+");

        public ParsePerformanceFilesImpl(Map<String, byte[]> inputByteMap) {
            super(inputByteMap);
        }

        @Override
        CompatibilityTestResult readFile(byte[] fileBytes) throws IOException {
            if (fileBytes.length == 0) {
                throw new BaseException("解析压力测试硬盘文件缺失");
            }
            CompatibilityTestResult testResult =
                    new CompatibilityTestResult(Constants.CompatibilityTestName.PRESSURE_NET.getTestName());
            Map<String, Map<String, List<String>>> netIdleMap = getIdleMap(fileBytes);
            if (netIdleMap.size() > 0) {
                XxkbRate rxkbRate = new XxkbRate();
                XxkbRate txkbRate = new XxkbRate();
                for (Map.Entry<String, Map<String, List<String>>> entry : netIdleMap.entrySet()) {
                    if (entry.getValue().get("rxkb").size() < 0 || entry.getValue().get("txkb").size() < 0) {
                        continue;
                    }

                    List<Double> rxkbDoubleList = entry.getValue().get("rxkb").stream()
                            .map(Double::parseDouble).collect(Collectors.toList());
                    calculateXxkbRate(rxkbDoubleList, entry.getKey(), rxkbRate);

                    List<Double> txkbDoubleList = entry.getValue().get("txkb")
                            .stream().map(Double::parseDouble).collect(Collectors.toList());

                    calculateXxkbRate(txkbDoubleList, entry.getKey(), txkbRate);
                }
                testResult = getTestCaseResult(rxkbRate, txkbRate, netIdleMap);
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(2), NIC_CN));
                testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(2), NIC_EN));
            }
            return testResult;
        }

        private void calculateXxkbRate(List<Double> rxkbDoubleList, String key, XxkbRate rate) {
            Double maxRxkb = Collections.max(rxkbDoubleList);
            rxkbDoubleList.remove(maxRxkb);
            Double minRxkb = Collections.min(rxkbDoubleList);
            rxkbDoubleList.remove(minRxkb);
            double rxkbAvg = rxkbDoubleList.stream().mapToDouble(Double::valueOf).average().orElse(0.00);
            if (Math.round(rxkbAvg) > 0) {
                BigDecimal num = new BigDecimal(8).divide(new BigDecimal(1000 * 1024));
                rate.setNetXxkbRate(BigDecimal.valueOf(Collections.max(rxkbDoubleList))
                        .subtract(new BigDecimal(String.valueOf(rxkbAvg))).abs().multiply(num).doubleValue());
            } else {
                rate.setNetXxkbRate(0.0);
            }
            if (rate.getMaxXxkbRate() <= rate.getNetXxkbRate()) {
                rate.setMaxXxkbDiskName(key);
                rate.setMaxXxkbRate(rate.getNetXxkbRate());
                rate.setNetXxkbMax(String.format(Locale.ROOT, "%.2f", Collections.max(rxkbDoubleList)));
                rate.setNetXxkbMin(String.format(Locale.ROOT, "%.2f", Collections.min(rxkbDoubleList)));
            }
        }

        private CompatibilityTestResult getTestCaseResult(XxkbRate rxkb, XxkbRate txkb,
                                                          Map<String, Map<String, List<String>>> netIdleMap) {
            int[] kbPass = {0, 0};
            if (rxkb.getMaxXxkbRate() <= 0.05) {
                kbPass[0] = 1;
            }
            if (txkb.getMaxXxkbRate() <= 0.05) {
                kbPass[1] = 1;
            }

            List<String> evidence = evidenceInit(rxkb, txkb, netIdleMap);
            List<String> evidenceEn = new ArrayList<>(evidence);
            Double maxRxkb = new BigDecimal(String.valueOf(rxkb.getMaxXxkbRate()))
                    .multiply(new BigDecimal(100)).doubleValue();
            Double maxTxkb = new BigDecimal(String.valueOf(txkb.getMaxXxkbRate()))
                    .multiply(new BigDecimal(100)).doubleValue();
            CompatibilityTestResult testResult =
                    new CompatibilityTestResult(Constants.CompatibilityTestName.PRESSURE_NET.getTestName());
            if (kbPass[0] == 1 && kbPass[1] == 1) {
                testResult.setResult(Constants.TEST_PASSED);
                evidence.add(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_LIST.get(4), "接收",
                        maxRxkb) + String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_LIST.get(5), "发送",
                        maxTxkb, "小于"));
                evidenceEn.add(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_EN_LIST.get(4), "received",
                        maxRxkb) + String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_EN_LIST.get(5), "sent",
                        maxTxkb, "less"));
            } else {
                testResult.setResult(Constants.TEST_FAILED);
                testResult.setReason(String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_LIST.get(4), "接收",
                        maxRxkb) + String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_LIST.get(5), "发送",
                        maxTxkb, "大于"));
                testResult.setReasonEn(String.format(Locale.ROOT, Constants.PERFORMANCE_DESC_EN_LIST.get(4),
                        "received", maxRxkb) + String.format(Locale.ROOT,
                        Constants.PERFORMANCE_DESC_EN_LIST.get(5), "sent",
                        maxTxkb, "greater"));
                evidence.add(testResult.getResult());
                evidenceEn.add(testResult.getReasonEn());
            }
            testResult.setEvidence(evidence);
            testResult.setEvidenceEn(evidenceEn);
            return testResult;
        }

        private List<String> evidenceInit(XxkbRate rxkb, XxkbRate txkb,
                                          Map<String, Map<String, List<String>>> netIdleMap) {
            List<String> rxkbEvidenceList = netIdleMap.get(rxkb.getMaxXxkbDiskName()).get("evidence");
            List<String> txkbEvidenceList = netIdleMap.get(txkb.getMaxXxkbDiskName()).get("evidence");

            List<String> evidence = new ArrayList<>();
            String finalNetRxkbMax = rxkb.getNetXxkbMax();
            evidence.add(rxkbEvidenceList.stream().filter(s -> StringUtils.containsIgnoreCase(s, finalNetRxkbMax))
                    .limit(1).collect(Collectors.joining()));
            String finalNetRxkbMin = rxkb.getNetXxkbMin();
            evidence.add(rxkbEvidenceList.stream().filter(s -> StringUtils.containsIgnoreCase(s, finalNetRxkbMin))
                    .limit(1).collect(Collectors.joining()));
            String finalNetTxkbMax = txkb.getNetXxkbMax();
            evidence.add(txkbEvidenceList.stream().filter(s -> StringUtils.containsIgnoreCase(s, finalNetTxkbMax))
                    .limit(1).collect(Collectors.joining()));
            String finalNetTxkbMin = txkb.getNetXxkbMin();
            evidence.add(txkbEvidenceList.stream().filter(s -> StringUtils.containsIgnoreCase(s, finalNetTxkbMin))
                    .limit(1).collect(Collectors.joining()));
            return evidence;
        }

        @Override
        List<String> getIdleList(byte[] fileBytes) throws IOException {
            return new ArrayList<>();
        }

        @Override
        Map<String, Map<String, List<String>>> getIdleMap(byte[] fileBytes) throws IOException {
            Map<String, Map<String, List<String>>> averageMap = new HashMap<>();
            try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
                 BufferedReader beginReader = new BufferedReader(
                        new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
                String line;

                while ((line = beginReader.readLine()) != null) {
                    if(StringUtils.containsIgnoreCase(line, "IFACE") || StringUtils.containsIgnoreCase(line, "lo")){
                        continue;
                    }
                    if (!idlePattern.matcher(line).find()){
                        continue;
                    }
                    Matcher matcher = pattern.matcher(line);
                    String netName = line.substring(13).trim().split(" ")[0];
                    List<String> matchList = new ArrayList<>();
                    while (matcher.find()) {
                        matchList.add(matcher.group().trim());
                    }
                    Map<String, List<String>> netMap = averageMap.getOrDefault(netName, new HashMap<>());
                    List<String> rxkbList = netMap.getOrDefault("rxkb", new ArrayList<>());
                    rxkbList.add(matchList.get(2));
                    List<String> txkbList = netMap.getOrDefault("txkb", new ArrayList<>());
                    txkbList.add(matchList.get(3));
                    List<String> evidenceList = netMap.getOrDefault("evidence", new ArrayList<>());
                    evidenceList.add(line);
                    netMap.put("rxkb", rxkbList);
                    netMap.put("txkb", txkbList);
                    netMap.put("evidence", evidenceList);
                    averageMap.put(netName, netMap);
                }
            }
            return averageMap;
        }
    }
}