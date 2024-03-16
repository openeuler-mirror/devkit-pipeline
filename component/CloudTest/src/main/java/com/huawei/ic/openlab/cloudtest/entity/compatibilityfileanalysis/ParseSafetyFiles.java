/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
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
import java.util.List;
import java.util.Locale;

/**
 * ParseSafetyFiles
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
@Slf4j
public class ParseSafetyFiles {
    private final byte[] protocolBytes;
    private final byte[] udpBytes;
    private final byte[] tcpBytes;

    /**
     * construction function
     *
     * @param protocolBytes bytes
     * @param udpBytes udp bytes
     * @param tcpBytes tcp bytes
     */
    public ParseSafetyFiles(byte[] protocolBytes, byte[] udpBytes, byte[] tcpBytes) {
        this.protocolBytes = protocolBytes;
        this.udpBytes = udpBytes;
        this.tcpBytes = tcpBytes;
    }

    /**
     * get average
     *
     * @param fileBytes file bytes
     * @return average list
     * @throws IOException exception
     */
    public List<String> getAverage(byte[] fileBytes) throws IOException {
        List<String> averageList = new ArrayList<>();
        try (InputStream beginInput = new ByteArrayInputStream(fileBytes);
             BufferedReader beginReader = new BufferedReader(
                    new InputStreamReader(beginInput, StandardCharsets.UTF_8))) {
            String line;
            while ((line = beginReader.readLine()) != null) {
                if (!StringUtils.startsWith(line, "#")
                        || StringUtils.containsIgnoreCase(line, "# Ports scanned")) {
                    averageList.add(line.trim());
                }
            }
        }
        return averageList;
    }

    /**
     *  read safety file
     *
     * @return CompatibilityTestResult
     * @throws IOException exception
     */
    public CompatibilityTestResult readSafetyFile() throws IOException {
        if (protocolBytes.length == 0 || udpBytes.length == 0 || tcpBytes.length == 0) {
            throw new BaseException("端口扫描文件缺失");
        }
        List<String> protocolList = getAverage(protocolBytes);
        List<String> tcpList = getAverage(tcpBytes);
        List<String> udpList = getAverage(udpBytes);

        CompatibilityTestResult testResult =
                new CompatibilityTestResult(Constants.CompatibilityTestName.SECURITY_PORT.getTestName());
        if (protocolList.size() > 0 && tcpList.size() > 0 && udpList.size() > 0) {
            testResult.setResult(Constants.TEST_PASSED);
            List<String> evidenceList = new ArrayList<>();
            List<String> evidenceEnList = new ArrayList<>();
            evidenceList.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(5), "PROTOCOL"));
            evidenceList.addAll(protocolList);
            evidenceList.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(5), "UDP"));
            evidenceList.addAll(udpList);
            evidenceList.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_LIST.get(5), "TCP"));
            evidenceList.addAll(tcpList);
            testResult.setEvidence(evidenceList);
            evidenceEnList.add(String.format(Locale.ROOT,
                    Constants.COMPATIBILITY_DESC_EN_LIST.get(5), "PROTOCOL"));
            evidenceEnList.addAll(protocolList);
            evidenceEnList.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(5), "UDP"));
            evidenceEnList.addAll(udpList);
            evidenceEnList.add(String.format(Locale.ROOT, Constants.COMPATIBILITY_DESC_EN_LIST.get(5), "TCP"));
            evidenceEnList.addAll(tcpList);
            testResult.setEvidenceEn(evidenceEnList);
        } else {
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SAFETY_FILE_LOSS);
            testResult.setReasonEn(Constants.SAFETY_FILE_LOSS_EN);
        }
        return testResult;
    }

    /**
     * parseSafetyFile
     *
     * @return CompatibilityTestResult
     */
    public CompatibilityTestResult parseSafetyFile() {
        CompatibilityTestResult testResult =
                new CompatibilityTestResult(Constants.CompatibilityTestName.SECURITY_PORT.getTestName());
        try {
            testResult = readSafetyFile();
        } catch (BaseException ex) {
            log.error("Missing security file", ex.getMessage());
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(Constants.SAFETY_FILE_LOSS);
            testResult.setReasonEn(Constants.SAFETY_FILE_LOSS_EN);
        } catch (NumberFormatException | IOException ex) {
            log.error("Security file parsing error", ex);
            testResult.setResult(Constants.TEST_FAILED);
            testResult.setReason(String.format(Locale.ROOT,
                    Constants.COMPATIBILITY_DESC_LIST.get(2), "安全测试"));
            testResult.setReasonEn(String.format(Locale.ROOT,
                    Constants.COMPATIBILITY_DESC_EN_LIST.get(2), "security "
                            + "test"));
        }
        return testResult;
    }
}
