/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.service;

import com.huawei.ic.openlab.cloudtest.entity.CloudLabTestTask;
import com.huawei.ic.openlab.cloudtest.entity.SystemParams;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.ParseFunctionTestLog;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.ParseInfoLog;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.ParseSafetyFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseClamLog;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseCpuComFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseCpuPerFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseCveCheckFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseDiskComFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseDiskPerFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseMemComFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseMemPerFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseNetComFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseNetPerFiles;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.impl.ParseSoftwareLog;
import com.huawei.ic.openlab.cloudtest.util.Constants;
import com.huawei.ic.openlab.cloudtest.util.ToolUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * TarFileService
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
@Service
@Slf4j
public class TarFileService {
    /**
     * BUFFER
     */
    public static final int BUFFER = 512;

    /**
     * TOOBIG
     */
    public static final int TOOBIG = 0X64000000;

    private final SystemParams systemParams;

    /**
     * TarFileService
     *
     * @param systemParams system Params
     */
    public TarFileService(SystemParams systemParams) {
        this.systemParams = systemParams;
    }

    /**
     * validatedTarGzFileSize
     *
     * @param fileInputStream input stream
     * @throws IOException exception
     */
    public void validatedTarGzFileSize(InputStream fileInputStream) throws IOException {
        long total = 0L;
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fileInputStream))) {
            while (zis.getNextEntry() != null) {
                total = getTarTotalSize(total, zis);
                if (total > TOOBIG) {
                    throw new IllegalStateException(Constants.TAR_FILE_TOO_BIG);
                }
            }
        }
    }

    private long getTarTotalSize(long total, ZipInputStream zis) throws IOException {
        int count;
        byte[] data = new byte[BUFFER];
        long sum = total;
        while ((count = zis.read(data, 0, BUFFER)) != -1) {
            sum += count;
            if (sum > TOOBIG) {
                break;
            }
        }
        return sum;
    }

    /**
     * validatedZipFileSize
     *
     * @param fileInputStream input stream
     * @param language language
     * @throws IOException exception
     */
    public void validatedZipFileSize(InputStream fileInputStream, String language) throws IOException {
        long total = 0L;
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(fileInputStream))) {
            while (zis.getNextEntry() != null) {
                total = getZipTotalSize(total, zis);
                if (total > TOOBIG) {
                    throw new IllegalStateException(
                            Constants.TAR_FILE_TOO_BIG_MAP.getOrDefault(language, "CN"));
                }
            }
        }
    }

    private long getZipTotalSize(long total, ZipArchiveInputStream zis) throws IOException {
        int count;
        byte[] data = new byte[BUFFER];
        long sum = total;
        while ((count = zis.read(data, 0, BUFFER)) != -1) {
            sum += count;
            if (sum > TOOBIG) {
                break;
            }
        }
        return sum;
    }

    /**
     * 应用启动、停止、异常测试用例
     *
     * @param filePath file path
     * @param testResultList test result list
     * @throws IOException exception
     */
    private void getTestCaseFromInfoLog(String filePath, List<CompatibilityTestResult> testResultList)
            throws IOException {
        byte[] infoLogFile = readSpecificFile(filePath, Constants.CompatibilityTarFile.INFO_LOG.getFileName());
        ParseInfoLog parseInfoLog = new ParseInfoLog(infoLogFile, "");
        Map<String, CompatibilityTestResult> infoResultMap = parseInfoLog.parseInfoLog();
        for (Map.Entry<String, CompatibilityTestResult> entry : infoResultMap.entrySet()) {
            testResultList.add(entry.getValue());
        }
    }

    /**
     * 软件识别用例
     *
     * @param filePath file path
     * @param testResultList test result list
     * @throws IOException exception
     */
    private void getTestCaseFromProductName(String filePath, List<CompatibilityTestResult> testResultList)
            throws IOException {
        byte[] configFile = readSpecificFile(filePath, Constants.CompatibilityTarFile.CONFIGURE_INFO.getFileName());
        byte[] processFile = readSpecificFile(filePath, Constants.CompatibilityTarFile.PRODUCT_NAME.getFileName());
        ParseSoftwareLog softwareLog = new ParseSoftwareLog(configFile, processFile);
        CompatibilityTestResult softwareResult = softwareLog.parseFiles();
        testResultList.add(softwareResult);
    }

    /**
     * 空载测试用例
     *
     * @param filePath filePath
     * @param testResultList testResultList
     * @throws IOException IOException
     */
    private void getTestCaseFromIdleFile(String filePath, List<CompatibilityTestResult> testResultList)
            throws IOException {
        // 空载测试CPU 用例
        Map<String, byte[]> beginCpuComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_CPU_0.getFileName());
        Map<String, byte[]> endCpuComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_CPU_1.getFileName());
        CompatibilityTestResult cpuComResult = new ParseCpuComFiles(beginCpuComFile, endCpuComFile).parseFiles();
        testResultList.add(cpuComResult);

        // 空载测试内存用例
        Map<String, byte[]> beginMemComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_MEM_0.getFileName());
        Map<String, byte[]> endMemComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_MEM_1.getFileName());
        CompatibilityTestResult memComResult = new ParseMemComFiles(beginMemComFile, endMemComFile).parseFiles();
        testResultList.add(memComResult);

        Map<String, byte[]> beginDiskComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_DISK_0.getFileName());
        Map<String, byte[]> endDiskComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_DISK_1.getFileName());
        CompatibilityTestResult diskComResult = new ParseDiskComFiles(beginDiskComFile, endDiskComFile).parseFiles();
        testResultList.add(diskComResult);

        Map<String, byte[]> beginNetComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_NET_0.getFileName());
        Map<String, byte[]> endNetComFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.COMPATIBILITY_NET_1.getFileName());
        CompatibilityTestResult diskNetResult = new ParseNetComFiles(beginNetComFile, endNetComFile).parseFiles();
        testResultList.add(diskNetResult);
    }

    /**
     * 压力测试用例
     *
     * @param filePath filePath
     * @param testResultList testResultList
     * @throws IOException exception
     */
    private void getTestCaseFromPressureFile(String filePath, List<CompatibilityTestResult> testResultList)
            throws IOException {
        Map<String, byte[]> cpuPerFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.PERFORMANCE_CPU.getFileName());
        CompatibilityTestResult cpuPerTestResult = new ParseCpuPerFiles(cpuPerFile).parseFiles();
        testResultList.add(cpuPerTestResult);
        Map<String, byte[]> memPerFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.PERFORMANCE_MEM.getFileName());
        CompatibilityTestResult memPerTestResult = new ParseMemPerFiles(memPerFile).parseFiles();
        testResultList.add(memPerTestResult);

        Map<String, byte[]> diskPerFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.PERFORMANCE_DISK.getFileName());
        CompatibilityTestResult diskPerTestResult = new ParseDiskPerFiles(diskPerFile).parseFiles();
        testResultList.add(diskPerTestResult);
        Map<String, byte[]> netPerFile = getSpecificFile(filePath,
                Constants.CompatibilityTarFile.PERFORMANCE_NET.getFileName());
        CompatibilityTestResult netPerTestResult = new ParseNetPerFiles(netPerFile).parseFiles();
        testResultList.add(netPerTestResult);
    }

    private void getTestCaseFromSafeFile(List<String> fileList, String filePath,
                                         List<CompatibilityTestResult> testResultList) throws IOException {
        // 端口扫描
        byte[] protocolFile = new byte[0];
        byte[] updFile = new byte[0];
        byte[] tcpFile = new byte[0];

        for (String tarFileName : fileList) {
            if (StringUtils.endsWith(tarFileName, "gnmap")
                    && StringUtils.containsIgnoreCase(tarFileName, "PROTOCOL")) {
                protocolFile = readSpecificFile(filePath, tarFileName);
            } else if (StringUtils.endsWith(tarFileName, "gnmap")
                    && StringUtils.containsIgnoreCase(tarFileName, "UDP"
            )) {
                updFile = readSpecificFile(filePath, tarFileName);
            } else if (StringUtils.endsWith(tarFileName, "gnmap")
                    && StringUtils.containsIgnoreCase(tarFileName, "TCP"
            )) {
                tcpFile = readSpecificFile(filePath, tarFileName);
            } else {
                log.info("not contrast");
            }
        }
        testResultList.add(new ParseSafetyFiles(protocolFile, updFile, tcpFile).parseSafetyFile());
    }

    /**
     * compatibility Result Analysis
     *
     * @param task task
     * @param multipartFile multipartFile
     * @return CompatibilityTestResult list
     */
    public List<CompatibilityTestResult> compatibilityResultAnalysis(String multipartFile) {

        String logPath = multipartFile;
        File file = FileUtils.getFile(logPath);
        List<CompatibilityTestResult> testResultList = new ArrayList<>();
        try {
            List<String> fileList = getTarFileList(logPath);
            getTestCaseFromInfoLog(logPath, testResultList);

            // 硬件识别用例
            CompatibilityTestResult testResult =
                    new CompatibilityTestResult(Constants.CompatibilityTestName.HARDWARE_SERVER.getTestName());
            testResult.setResult(Constants.TEST_PASSED);
            testResult.setReason(Constants.HCS_DESC);
            testResultList.add(testResult);

            getTestCaseFromProductName(logPath, testResultList);
            getTestCaseFromIdleFile(logPath, testResultList);
            getTestCaseFromPressureFile(logPath, testResultList);
            getTestCaseFromSafeFile(fileList, logPath, testResultList);

            // 功能测试
            byte[] shellBytes = readSpecificFile(logPath,
                    Constants.CompatibilityTarFile.FUNCTION_SHELL_UNIT.getFileName());
            byte[] pytestBytes = readSpecificFile(logPath,
                    Constants.CompatibilityTarFile.FUNCTION_PY_TEST.getFileName());
            testResultList.add(new ParseFunctionTestLog(shellBytes, pytestBytes).parseFunctionTestLog());
        } catch (IOException ex) {
            log.error("Error parsing tar file", ex);
        }
        return testResultList;
    }

    /**
     * get tar file list
     *
     * @param tarGzFile tar file
     * @return file list
     * @throws IOException exception
     */
    public List<String> getTarFileList(String tarGzFile) throws IOException {
        ToolUtil.validPath(tarGzFile);
        List<String> fileList = new ArrayList<>();
        try (FileInputStream fin = new FileInputStream(tarGzFile);
             BufferedInputStream in = new BufferedInputStream(fin);
             GzipCompressorInputStream gzip = new GzipCompressorInputStream(in);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzip)) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                fileList.add(entry.getName());
            }
        }
        return fileList;
    }

    /**
     * get specific file
     *
     * @param tarGzFile tar file
     * @param fileName file name
     * @return file map
     * @throws IOException exception
     */
    public Map<String, byte[]> getSpecificFile(String tarGzFile, String fileName) throws IOException {
        ToolUtil.validPath(tarGzFile);
        Map<String, byte[]> fileMap = new HashMap<>();
        try (FileInputStream fin = new FileInputStream(tarGzFile);
             BufferedInputStream in = new BufferedInputStream(fin);
             GzipCompressorInputStream gzip = new GzipCompressorInputStream(in);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzip)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                if (StringUtils.startsWithIgnoreCase(entry.getName(), fileName)) {
                    byte[] fileBytes = new byte[(int) entry.getSize()];
                    tarIn.read(fileBytes);
                    fileMap.put(entry.getName(), fileBytes);
                }
            }
        }
        return fileMap;
    }

    /**
     * read specific file
     *
     * @param tarGzFile tar file
     * @param fileName file name
     * @return byte []
     * @throws IOException exception
     */
    public byte[] readSpecificFile(String tarGzFile, String fileName) throws IOException {
        ToolUtil.validPath(tarGzFile);
        byte[] fileBytes = new byte[0];
        try (FileInputStream fin = new FileInputStream(tarGzFile);
             BufferedInputStream in = new BufferedInputStream(fin);
             GzipCompressorInputStream gzip = new GzipCompressorInputStream(in);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzip)) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                if (StringUtils.startsWithIgnoreCase(entry.getName(), fileName)) {
                    fileBytes = new byte[(int) entry.getSize()];
                    tarIn.read(fileBytes);
                }
            }
        }
        return fileBytes;
    }
}