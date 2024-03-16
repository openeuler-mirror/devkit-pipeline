/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.service;

import com.alibaba.fastjson.JSONArray;
import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.common.exception.SshErrorException;
import com.huawei.ic.openlab.cloudtest.entity.CloudLabTestTask;
import com.huawei.ic.openlab.cloudtest.entity.LabTestReq;
import com.huawei.ic.openlab.cloudtest.entity.MqsMessage;
import com.huawei.ic.openlab.cloudtest.entity.ScriptResultConfig;
import com.huawei.ic.openlab.cloudtest.entity.SystemParams;
import com.huawei.ic.openlab.cloudtest.entity.TestCaseResult;
import com.huawei.ic.openlab.cloudtest.entity.TestCaseResultCount;
import com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis.CompatibilityTestResult;
import com.huawei.ic.openlab.cloudtest.util.Constants;
import com.huawei.ic.openlab.cloudtest.util.FileUtil;
import com.huawei.ic.openlab.cloudtest.util.NormalResp;
import com.huawei.ic.openlab.cloudtest.util.PerformanceApiClient;
import com.huawei.ic.openlab.cloudtest.util.RandomUtil;
import com.huawei.ic.openlab.cloudtest.util.ToolUtil;
import com.huawei.ic.openlab.cloudtest.util.fastdfs.FastDfsClient;
import com.huawei.ic.openlab.cloudtest.util.sshclient.SFTPUtil;
import com.huawei.ic.openlab.cloudtest.util.sshclient.SSHUtil;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * LabEnvService
 *
 * @author kongcaizhi
 * @since 2021-10-19
 */
@Service
@Slf4j
public class LabEnvService {
    @Autowired
    private SystemParams systemParams;
    @Autowired
    private TarFileService fileService;
    @Autowired
    private PerformanceApiClient performanceApiClient;
    @Autowired
    private FastDfsClient fastDfsClient;

    /**
     * getLabConnectTest
     *
     * @param ip ip
     * @param port port
     * @param userName userName
     * @param passWord password
     * @return boolean
     */
    public boolean getLabConnectTest(String ip, int port, String userName, String passWord) {
        return SSHUtil.sshConnectTest(ip, port, userName, passWord);
    }

    /**
     * validatedZipFileSize
     *
     * @param fileInputStream fileInputStream
     * @param language language
     * @throws IOException exception
     */
    public void validatedZipFileSize(InputStream fileInputStream, String language) throws IOException {
        fileService.validatedZipFileSize(fileInputStream, language);
    }

    /**
     * validatedTarZipFile
     *
     * @param fileInputStream fileInputStream
     * @throws IOException exception
     */
    public void validatedTarZipFile(InputStream fileInputStream) throws IOException {
        fileService.validatedTarGzFileSize(fileInputStream);
    }


    /**
     * testPreparation
     *
     * @param testReq testReq
     * @throws SshErrorException SshErrorException
     * @throws FileNotFoundException FileNotFoundException
     */
    public void testPreparation(LabTestReq testReq) throws SshErrorException, FileNotFoundException {
        List<String> commandList = new ArrayList<>();
        commandList.add("mkdir -p /home/compatibility_testing; mkdir -p /home/function_testing");
        String wgetCom = "cd /home/compatibility_testing; wget http://%s:8080/download/compatibility_testing.tar.gz";
        commandList.add(String.format(Locale.ROOT, wgetCom, systemParams.getDeployIP()));
        commandList.add("cd /home/compatibility_testing; tar -xvzf compatibility_testing.tar.gz");
        String wgetFun = "cd /home/function_testing; wget http://%s:8080/download/shunit2-master.zip";
        commandList.add(String.format(Locale.ROOT, wgetFun, systemParams.getDeployIP()));
        commandList.add("cd /home/function_testing; unzip -o -q shunit2-master.zip");
        SSHUtil.sshExecCmd(testReq.getServerIp(), testReq.getServerPort(), testReq.getServerUser(),
                testReq.getServerPassword(), commandList);
    }

    /**
     * 修改配置文件,并上传到实验室环境
     *
     * @param testReq testReq
     * @throws IOException IOException
     * @throws JSchException JSchException
     * @throws SftpException SftpException
     */
    private void setConfigFile(LabTestReq testReq) throws IOException, JSchException, SftpException {
        String configContent = readConfigFile(testReq);
        String configFileName = writeConfigFile(configContent);
        ToolUtil.validPath(configFileName);
        File configFile = new File(configFileName);
        try (InputStream in = Files.newInputStream(configFile.toPath())) {
            String targetPath = String.format(Locale.ROOT, "/home/compatibility_testing/compatibility_testing/%s"
                    + "/compatibility_testing.conf", "CN".equals(testReq.getTaskLanguage()) ? "Chinese" : "English");
            SFTPUtil.uploadFile(testReq, targetPath, in);
        }
        log.info("Delete configure file {},result {}", configFileName, configFile.delete());
    }

    private String readConfigFile(LabTestReq testReq) {
        StringBuilder res = new StringBuilder(16);
        try (BufferedReader reader = new BufferedReader(
                new FileReader(systemParams.getScriptConfig(testReq.getTaskLanguage())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.startsWithIgnoreCase(line, "application_names=")) {
                    line = line + testReq.getApplicationNames();
                }
                if (StringUtils.startsWithIgnoreCase(line, "start_app_commands=")) {
                    line = line + testReq.getStartAppCommands();
                }
                if (StringUtils.startsWithIgnoreCase(line, "stop_app_commands=")) {
                    line = line + testReq.getStopAppCommands();
                }
                if (StringUtils.startsWithIgnoreCase(line, "application_install_dir=")) {
                    line = line + testReq.getDeployDir();
                }
                res.append(line).append(System.lineSeparator());
            }
        } catch (FileNotFoundException ex) {
            log.error(ex.getLocalizedMessage());
            throw new BaseException("配置文件不存在");
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return res.toString();
    }

    private String writeConfigFile(String content) {
        SecureRandom random = RandomUtil.getRandom();
        int ends = random.nextInt(99);
        String fileName = ToolUtil.getTimeStr() + String.format(Locale.ROOT, "%02d", ends);
        fileName = systemParams.getTempDir() + File.separator + fileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return fileName;
    }

    private void setStep(String step, String resultCode, String timeString, CloudLabTestTask.StepStatus stepStatus) {
        switch (step) {
            case "01":
                stepStatus.setStep1(resultCode, timeString);
                break;
            case "02":
                stepStatus.setStep2(resultCode, timeString);
                break;
            case "03":
                stepStatus.setStep3(resultCode, timeString);
                break;
            case "04":
                stepStatus.setStep4(resultCode, timeString);
                break;
            case "05":
                stepStatus.setStep5(resultCode, timeString);
                break;
            case "06":
                stepStatus.setStep6(resultCode, timeString);
                break;
            case "07":
                stepStatus.setStep7(resultCode, timeString);
                break;
            case "08":
                stepStatus.setStep8(resultCode, timeString);
                break;
            case "09":
                stepStatus.setStep9(resultCode, timeString);
                break;
            default:
                stepStatus.setStep10(resultCode, timeString);
                break;
        }
    }

    private void setTestTime(String step, String timeString, CloudLabTestTask.TestBeginTime testBeginTime,
                             boolean isFunctionTest) {
        if ("05".equals(step)) {
            testBeginTime.setCompatibilityTime(timeString);
            testBeginTime.setSecurityTime(timeString);
            testBeginTime.setReliabilityTime(timeString);
            testBeginTime.setFunctionTime(timeString);
        } else if ("06".equals(step)) {
            testBeginTime.setCompatibilityTime(timeString);
            testBeginTime.setReliabilityTime(timeString);
        } else if ("08".equals(step)) {
            testBeginTime.setCompatibilityTime(timeString);
        } else if ("09".equals(step) && isFunctionTest) {
            testBeginTime.setFunctionTime(timeString);
        } else {
            log.info("not contrast");
        }
    }

    /**
     * 组装测试进度推送MQS 消息
     *
     * @param task task
     * @param resultCode resultCode
     * @param timeString timeString
     * @return MqsMessage object
     */
    private MqsMessage getProgressMessage(CloudLabTestTask task, String resultCode, String timeString) {
        MqsMessage message = new MqsMessage();
        message.setProjectId(task.getProjectId());
        message.setUserId(task.getUserId());
        message.setServerIp(task.getServerIp());
        message.setStatusTime(timeString);
        message.setStatus(Constants.MQS_STATUS_ONGOING);
        message.setStatusDesc("");

        MqsMessage.MessageDetail detail = setDetail(task, resultCode);
        message.setDetail(detail);
        return message;
    }

    private MqsMessage.MessageDetail setDetail(CloudLabTestTask task, String resultCode) {
        MqsMessage.MessageDetail detail = new MqsMessage.MessageDetail();
        switch (Objects.requireNonNull(Constants.CompatibilityTestStep.fromStepIndex(resultCode))) {
            case DEPENDENCY_INSTALL_SUCCESS:
                detail.setDependencyInstallSuccess();
                break;
            case APP_START_SUCCESS:
                detail.setAppStartSuccess();
                break;
            case APP_STOP_SUCCESS:
                detail.setAppStopSuccess(task.isCompatibilityTest(),
                        task.isReliabilityTest(), task.isSecurityTest());
                break;
            case COMPATIBILITY_TEST_SUCCESS:
            case COM_START_SUCCESS:
                detail.setComStartSuccess(task.isReliabilityTest(), task.isSecurityTest());
                if ((task.isCompatibilityTest() || task.isReliabilityTest())
                        && !task.isSecurityTest() && task.getPerformanceFile() != null) {
                    setPerformanceTest(task, true);
                }
                break;
            case PORT_TEST_SUCCESS:
                detail.setPortTestSuccess();
                break;
            case VIRUS_SCAN_SUCCESS:
                detail.setVirusScanSuccess(task.isCompatibilityTest(), task.isReliabilityTest());
                if ((task.isCompatibilityTest() || task.isReliabilityTest()) && task.getPerformanceFile() != null) {
                    setPerformanceTest(task, true);
                }
                break;
            case VULNERABLE_SCAN_SUCCESS:
                detail.setVulnerableScanSuccess(task.isCompatibilityTest(), task.isReliabilityTest());
                break;
            case EXCEPTION_TEST_FINISH:
                detail.setExceptionTestFinish(task.isCompatibilityTest());
                break;
            case COMPATIBILITY_TEST_FINISH:
                detail.setStep(Constants.COMPATIBILITY_TEST_DESC);
                detail.setStepStatus(Constants.TEST_FINISHED_CN);
                break;
            case FUN_TEST_FILE_EXIST_ERROR:
            case FUN_TEST_FILE_DIR_ERROR:
            case FUN_TEST_PYTEST_ERROR:
                detail.setStep(Constants.FUNCTION_TEST_DESC);
                detail.setStepStatus(Constants.TEST_FAILURE_CN);
                break;
            default:
                break;
        }
        return detail;
    }

    private void sendExceptionMessage(CloudLabTestTask task, String timeString, MqsMessage.MessageDetail detail) {
        MqsMessage message = new MqsMessage();
        message.setProjectId(task.getProjectId());
        message.setUserId(task.getUserId());
        message.setServerIp(task.getServerIp());
        message.setStatusTime(timeString);
        message.setStatus(Constants.MQS_STATUS_EXCEPTION);
        message.setStatusDesc(detail.getExceptionType());
        message.setDetail(detail);
    }

    private void ransfer2json(List<TestCaseResult> input_info, String path) {
        String jsonArray = JSONArray.toJSONString(input_info);
        byte[] temp_result = jsonArray.getBytes(StandardCharsets.UTF_8);
        try(FileOutputStream fos=new FileOutputStream(new File(path));
        ByteArrayInputStream bais = new ByteArrayInputStream(temp_result)){
            IOUtils.copy(bais, fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resultJson(String multipartFile, String savePath) {
        List<TestCaseResult> testCaseResultList = null;
        List<CompatibilityTestResult> results = fileService.compatibilityResultAnalysis(multipartFile);
        if (results.size() != 0){
            testCaseResultList = analyseCompatibilityTestResult(results, "CN");
        } else {
            throw new BaseException("解析兼容性测试工具输出日志异常");
        }
        ransfer2json(testCaseResultList, savePath);
    }

    private String resultFileAnalysis(CloudLabTestTask task, MultipartFile multipartFile, String resultCode,
                                      String fileStatus, String timeString) {
        List<TestCaseResult> temp = null;
        CloudLabTestTask.StepStatus stepStatus = task.getStepStatus();
        if (stepStatus == null) {
            stepStatus = new CloudLabTestTask.StepStatus();
        }
        stepStatus.setStep10Code(resultCode);
        stepStatus.setStep10Time(timeString);
        stepStatus.setStep10File(getFileFromRequest(multipartFile, "兼容性测试工具输出日志", fileStatus));
        if (!task.isPerformanceTest() || (task.getPerformanceFile() == null
                && Constants.RELIABILITY_START_APP_FAIL_CODE.equals(stepStatus.getStep9Code()))) {
            task.setProjectStatus(Constants.CLOUD_LAB_TEST_FINISH);
            task.setFinishTime(timeString);

            task.setProjectStatus(Constants.CLOUD_LAB_TEST_FINISH);
        } else {
            stepStatus.setPerformanceTime(timeString);
        }
        if (multipartFile != null) {
            task.setResultFile(stepStatus.getStep10File());
            List<CompatibilityTestResult> results = fileService.compatibilityResultAnalysis(multipartFile.getOriginalFilename());
            if (results.size() != 0) {
                temp = analyseCompatibilityTestResult(results, "CN");
            } else {
                task.setStatusDesc("解析兼容性测试工具输出日志异常");
                task.setProjectStatus(Constants.CLOUD_LAB_TEST_EXCEPTION);
            }
        } else {
            task.setStatusDesc("兼容性测试工具输出日志," + fileStatus);
            task.setProjectStatus(Constants.CLOUD_LAB_TEST_EXCEPTION);
        }

        task.setStepStatus(stepStatus);

        return NormalResp.ok();
    }

    private void setPerformanceTest(CloudLabTestTask task, boolean isTimeLimited) {
        try {
            String fileName = task.getPerformanceFile().getFilePath();
            MultipartFile multipartFile = FileUtil.fileToMultipartFile(fileName);
            String result = performanceApiClient.setPerformanceTest(createURI(task.getPerformanceService()),
                    task.getProjectId(), task.getServerIp(), isTimeLimited, multipartFile);
            log.info("Task {} request {} return from performance test {}", task.getProjectId(),
                    task.getPerformanceService(), result);
        } catch (URISyntaxException ex) {
            log.error("Failed to request performance test {}", ex.getLocalizedMessage());
        }
    }

    /**
     * 往MQS发送性能测试中状态
     *
     * @param task task
     * @param timeString timeString
     */
    private void sendPerformanceMQS(CloudLabTestTask task, String timeString) {
        MqsMessage message = new MqsMessage();
        message.setProjectId(task.getProjectId());
        message.setUserId(task.getUserId());
        message.setServerIp(task.getServerIp());
        message.setStatusTime(timeString);
        message.setStatus(Constants.MQS_STATUS_ONGOING);
        message.setStatusDesc("");
        MqsMessage.MessageDetail detail = new MqsMessage.MessageDetail();
        detail.setStep(Constants.PERFORMANCE_TEST_DESC);
        detail.setStepStatus(Constants.TEST_PROCESSING);
        message.setDetail(detail);
    }

    /**
     * 往MQS发送分析结果
     *
     * @param task task
     * @param timeString time string
     */
    private void sentResultMQSMessage(CloudLabTestTask task, String timeString) {
        MqsMessage message = new MqsMessage();
        message.setProjectId(task.getProjectId());
        message.setUserId(task.getUserId());
        message.setServerIp(task.getServerIp());
        message.setStatusTime(timeString);
        message.setStatus(Constants.MQS_STATUS_FINISH);
        MqsMessage.MessageDetail detail = new MqsMessage.MessageDetail();
        detail.setResultFileName(task.getResultFile().getFileName());
        detail.setFileStatus(Constants.TEST_NORMAL_CN);
        message.setDetail(detail);
        MqsMessage.MessageResult result = new MqsMessage.MessageResult();
        result.setScriptResultConfig(task.getScriptResultConfig());
        result.setTestDetail(task.getTestDetail());
        result.setTestSummary(task.getTestSummary());
        message.setTestResult(result);

    }

    /**
     * 上传的文件,存储到服务器,返回路径
     *
     * @param multipartFile multipartFile
     * @param type type
     * @param fileStatus fileStatus
     * @return 路径
     */
    private CloudLabTestTask.UploadFile getFileFromRequest(MultipartFile multipartFile, String type,
                                                           String fileStatus) {
        CloudLabTestTask.UploadFile uploadFile = new CloudLabTestTask.UploadFile();
        uploadFile.setFileDesc(type);
        uploadFile.setFileStatus(fileStatus);
        if (multipartFile == null) {
            return uploadFile;
        }
        try {
            SecureRandom random = RandomUtil.getRandom();
            String fileName = random.getAlgorithm() + "-"
                    + DigestUtils.md5DigestAsHex(multipartFile.getInputStream())
                    + multipartFile.getOriginalFilename().substring(
                            multipartFile.getOriginalFilename().lastIndexOf("."));
            String dirName = systemParams.getUploadFileDir() + File.separator
                    + ToolUtil.getDateStr() + File.separator + fileName;
            ToolUtil.validPath(dirName);
            File file = new File(dirName);
            FileUtils.copyToFile(multipartFile.getInputStream(), file);
            uploadFile.setFileId(DigestUtils.md5DigestAsHex(multipartFile.getInputStream()));
            uploadFile.setFileName(fileName);
            uploadFile.setFilePath(dirName);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage(), ex);
            uploadFile.setFileStatus(Constants.UPLOAD_FILE_STATUS_EXCEPTION);
        }
        return uploadFile;
    }

    private List<TestCaseResult> analyseCompatibilityTestResult(List<CompatibilityTestResult> results, String lan) {
        ScriptResultConfig config = new ScriptResultConfig();
        TestCaseResultCount functionCount = new TestCaseResultCount();
        TestCaseResultCount compatibilityCount = new TestCaseResultCount();
        TestCaseResultCount reliabilityCount = new TestCaseResultCount();
        TestCaseResultCount securityCount = new TestCaseResultCount();
        List<TestCaseResult> testCaseResults = new ArrayList<>();

        for (CompatibilityTestResult item : results) {
            ready(item, config);
            if (StringUtils.isNoneEmpty(item.getId()) && item.getId().startsWith(
                    "Compatibility")) {
                compatibilityCount.add(item.getResult());
                testCaseResults.add(getTestCaseResult(item, lan));
            } else if (StringUtils.isNoneEmpty(item.getId()) && item.getId().startsWith(
                    "Reliability")) {
                reliabilityCount.add(item.getResult());
                testCaseResults.add(getTestCaseResult(item, lan));
            } else if (StringUtils.isNoneEmpty(item.getId()) && item.getId().startsWith(
                    "Security")) {
                securityCount.add(item.getResult());
                testCaseResults.add(getTestCaseResult(item, lan));
            } else {
                log.info("not contrast");
            }
        }
        CloudLabTestTask.TestCaseSummary testCaseSummary = new CloudLabTestTask.TestCaseSummary(compatibilityCount,
                reliabilityCount, securityCount, functionCount);

        return testCaseResults;
    }

    private void ready(CompatibilityTestResult item, ScriptResultConfig config) {
        if (StringUtils.isNoneEmpty(item.getId()) && StringUtils.equals(item.getId(),
                Constants.CompatibilityTestName.APPLICATION_START.getTestName())) {
            config.setOsVersion(item.getOsVersion());
        } else if (StringUtils.isNoneEmpty(item.getId()) && StringUtils.equals(item.getId(),
                Constants.CompatibilityTestName.SOFTWARE_NAME.getTestName())) {
            config.setApplicationNames(item.getApplicationNames() != null ? String.join(",",
                    item.getApplicationNames()) : "");
            config.setStartAppCommands(item.getStartAppCommands() != null ? String.join(",",
                    item.getStartAppCommands()) : "");
            config.setStopAppCommands(item.getStopAppCommands() != null ? String.join(",",
                    item.getStopAppCommands()) : "");
        } else {
            log.info("not contrast");
        }
    }

    private TestCaseResult createTestCaseResult(CloudLabTestTask.UploadFile performanceFile, String taskLanguage) {
        TestCaseResult testCaseResult = new TestCaseResult();
        testCaseResult.setId(Constants.PERFORMANCE_TEST_ID);
        testCaseResult.setResult(Constants.TEST_FAILED);
        if (performanceFile == null) {
            testCaseResult.setReason(Constants.PERFORMANCE_FILE_MISSING_MAP.get(taskLanguage));
        } else {
            testCaseResult.setReason(Constants.APP_START_UP_FAILURE_DESC_MAP.get(taskLanguage));
        }
        return testCaseResult;
    }

    private void setFunctionCount(CompatibilityTestResult item, TestCaseResultCount functionCount,
                                  CloudLabTestTask.StepStatus status, CloudLabTestTask.TestBeginTime testBeginTime) {
        if (Constants.TEST_PASSED.equals(item.getResult())) {
            JSONObject jsonObject = JSONObject.parseObject(item.getReason());
            functionCount.setPassed(jsonObject.getIntValue(Constants.TEST_PASSED));
            functionCount.setFailed(jsonObject.getIntValue(Constants.TEST_FAILED));
            functionCount.setTotal(jsonObject.getIntValue(Constants.TEST_PASSED)
                    + jsonObject.getIntValue(Constants.TEST_FAILED));
        }
        if (Constants.RELIABILITY_START_APP_FAIL_CODE.equals(status.getStep9Code())) {
            functionCount.setPassed(0);
            functionCount.setFailed(0);
            functionCount.setTotal(0);
        }
        functionCount.setStartTime(testBeginTime.getFunctionTime());
    }

    private TestCaseResult getTestCaseResult(CompatibilityTestResult testResult, String language) {
        TestCaseResult testCaseResult = new TestCaseResult();
        testCaseResult.setId(testResult.getId());
        testCaseResult.setResult(testResult.getResult());
        testCaseResult.setReason("CN".equals(language) ? testResult.getReason() : testResult.getReasonEn());
        testCaseResult.setEvidence(String.join(",", "CN".equals(language)
                ? testResult.getEvidence() : testResult.getEvidenceEn()));
        return testCaseResult;
    }

    /**
     * 上传功能测试文件到远程实验室
     *
     * @param task task
     * @param uploadFile upload file
     */
    private void setRemoteFunctionFile(CloudLabTestTask task, CloudLabTestTask.UploadFile uploadFile) {
        ToolUtil.validPath(uploadFile.getFilePath());
        try (InputStream in = new FileInputStream(uploadFile.getFilePath())) {
            SFTPUtil.uploadFile(task, "/home/function_testing/function_testing.zip", in);
        } catch (JSchException | SftpException | IOException ex) {
            log.error("Failed to upload task {} function file {} to remote lab {},{}", task.getProjectId(),
                    uploadFile.getFilePath(), task.getServerIp(), ex.getLocalizedMessage());
        }
    }

    private boolean stopCompatibilityTesting(CloudLabTestTask task) {
        List<String> commandList = new ArrayList<>();
        commandList.add("pid=$(pgrep -f sh\\ compatibility_testing.sh|head -1);sub_pids=$(pgrep -P ${pid});for "
                + "subpid" + " " + "in ${sub_pids};do kill -9  \"${subpid}\" ;done;kill -9  \"${pid}\" ");
        try {
            SSHUtil.sshExecCmd(task.getServerIp(), task.getServerPort(), task.getServerUser(),
                    task.getServerPassword(), commandList);

            return true;
        } catch (SshErrorException ex) {
            log.error("Failed to execute task {},{}", task.getProjectId(), ex.getLocalizedMessage());
            return false;
        }
    }

    private boolean stopPerformanceTesting(CloudLabTestTask task) {
        try {
            String result = performanceApiClient.stopPerformanceTest(createURI(task.getPerformanceService()),
                    task.getProjectId());
            log.info("Task {} request {} stop performance test, return {}", task.getProjectId(),
                    task.getPerformanceService(), result);
            JSONObject resultJson = JSONObject.parseObject(result);
            return resultJson.containsKey("code") && "0000".equals(resultJson.getString("code"));
        } catch (URISyntaxException ex) {
            log.error("Failed to stop performance test {}", ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     * upload file
     *
     * @param file file
     * @return response
     */
    public String uploadFile(MultipartFile file) {
        String fileId = null;
        try {
            fileId = fastDfsClient.uploadFile(file);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        return fileId;
    }

    private URI createURI(String param) throws URISyntaxException {
        ToolUtil.checkParameter(param);
        return new URI(String.format(Locale.ROOT,
                systemParams.getPerformanceTestUrl(), param));
    }
}
