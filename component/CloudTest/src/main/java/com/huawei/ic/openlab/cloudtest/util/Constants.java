/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Constants
 *
 * @author kongcaizhi
 * @since 2022-10-24
 */
public final class Constants {
    /**
     * TEST_PASSED
     */
    public static final String TEST_PASSED = "passed";

    /**
     * TEST_FAILED
     */
    public static final String TEST_FAILED = "failed";

    /**
     * TEST_SKIPPED
     */
    public static final String TEST_SKIPPED = "skipped";

    /**
     * CLOUD_LAB_TEST_SUBMIT
     */
    public static final Integer CLOUD_LAB_TEST_SUBMIT = 0;

    /**
     * CLOUD_LAB_TEST_START
     */
    public static final Integer CLOUD_LAB_TEST_START = 1;

    /**
     * CLOUD_LAB_TEST_FINISH
     */
    public static final Integer CLOUD_LAB_TEST_FINISH = 2;

    /**
     * CLOUD_LAB_TEST_EXCEPTION
     */
    public static final Integer CLOUD_LAB_TEST_EXCEPTION = 3;

    /**
     * CLOUD_LAB_TEST_HALT
     */
    public static final Integer CLOUD_LAB_TEST_HALT = 4;

    /**
     * TAR_FILE_TOO_BIG
     */
    public static final String TAR_FILE_TOO_BIG = "上传的文件解压后不能超过100M,请重新上传.";

    /**
     * FILE_TOO_BIG
     */
    public static final String FILE_TOO_BIG = "上传的文件不能超过100M,请重新上传.";

    /**
     * CONNECT_EXCEPTION_DESC
     */
    public static final String CONNECT_EXCEPTION_DESC = "连接异常,请检查登录密码.";

    /**
     * TASK_DUPLICATE
     */
    public static final String TASK_DUPLICATE = "%s服务器正在测试中,请勿重复提交.";

    /**
     * TEMPLATE_DECOMPRESSION_ERROR
     */
    public static final String TEMPLATE_DECOMPRESSION_ERROR = "请下载模板,根据模板修改,上传修改后的压缩文件";

    /**
     * FILE_FAILED_VIRUS_SCAN
     */
    public static final String FILE_FAILED_VIRUS_SCAN = "文件没有通过病毒扫描,请重新上传";

    /**
     * 正常
     */
    public static final String UPLOAD_FILE_STATUS_NORMAL = "00";

    /**
     * 已删除
     */
    public static final String UPLOAD_FILE_STATUS_DELETE = "01";

    /**
     * 安全检查不通过
     */
    public static final String UPLOAD_FILE_STATUS_SCAN_FAIL = "02";

    /**
     * 文件太大
     */
    public static final String UPLOAD_FILE_STATUS_TOO_BIG = "03";

    /**
     * 文件异常
     */
    public static final String UPLOAD_FILE_STATUS_EXCEPTION = "04";

    /**
     * 安全测试文件缺失或者读取失败
     */
    public static final String SAFETY_FILE_LOSS = "安全测试文件缺失或者读取失败";

    /**
     * SAFETY_FILE_LOSS_EN
     */
    public static final String SAFETY_FILE_LOSS_EN = "The security test file is missing or cannot be read";

    /**
     * HCS_DESC
     */
    public static final String HCS_DESC = "硬件包含鲲鹏芯片，符合要求";

    /**
     * PERFORMANCE_TEST_ID
     */
    public static final String PERFORMANCE_TEST_ID = "Performance_Test_1";

    /**
     * TASK_DOES_NOT_EXIST
     */
    public static final String TASK_DOES_NOT_EXIST = "任务不存在";

    /**
     * TASK_HAS_FINISHED
     */
    public static final String TASK_HAS_FINISHED = "测试已结束";

    /**
     * FAILED_TO_STOP_TASK
     */
    public static final String FAILED_TO_STOP_TASK = "停止测试任务失败";

    /**
     * PERFORMANCE_TESTING
     */
    public static final String PERFORMANCE_TESTING = "性能测试中";

    /**
     * START_TEST_STRING
     */
    public static final String START_TEST_STRING = "自动化测试采集工具开始执行";

    /**
     * START_TEST_STRING_EN
     */
    public static final String START_TEST_STRING_EN = "The automatic test tool starts to run.";

    /**
     * DEPENDENCY_INSTALL_FAILURE_DESC
     */
    public static final String DEPENDENCY_INSTALL_FAILURE_DESC = "依赖软件安装失败";

    /**
     * MQS_STATUS_ONGOING
     */
    public static final String MQS_STATUS_ONGOING = "ongoing";

    /**
     * MQS_STATUS_FINISH
     */
    public static final String MQS_STATUS_FINISH = "finished";

    /**
     * MQS_STATUS_EXCEPTION
     */
    public static final String MQS_STATUS_EXCEPTION = "exception";

    /**
     * 依赖软件安装
     */
    public static final String DEPENDENCY_INSTALL_DESC = "0-1";

    /**
     * 应用软件启动
     */
    public static final String APP_START_DESC = "0-2";

    /**
     * 应用软件停止
     */
    public static final String APP_STOP_DESC = "0-3";

    /**
     * 安全测试
     */
    public static final String SECURITY_TEST_DESC = "1";

    /**
     * 可靠性测试
     */
    public static final String RELIABILITY_TEST_DESC = "2";

    /**
     * 兼容性测试
     */
    public static final String COMPATIBILITY_TEST_DESC = "3";

    /**
     * 功能测试
     */
    public static final String FUNCTION_TEST_DESC = "4";

    /**
     * 性能测试
     */
    public static final String PERFORMANCE_TEST_DESC = "5";

    /**
     * 测试状态-中
     */
    public static final String TEST_PROCESSING = "0";

    /**
     * 测试状态-完成
     */
    public static final String TEST_FINISHED_CN = "1";

    /**
     * 测试状态-失败
     */
    public static final String TEST_FAILURE_CN = "2";

    /**
     * 测试状态-异常
     */
    public static final String TEST_EXCEPTION_CN = "异常";

    /**
     * T测试状态-正常
     */
    public static final String TEST_NORMAL_CN = "正常";

    /**
     * RELIABILITY_START_APP_FAIL_CODE
     */
    public static final String RELIABILITY_START_APP_FAIL_CODE = "0911";

    /**
     * OS_PATTERN_CN
     */
    public static final String OS_PATTERN_CN = "#\\d{4}-\\d{2}-\\d{2}\\s+(20|21|22|23|[0-1]\\d)"
            + ":[0-5]\\d:[0-5]\\d#info#1#(当前的操作系统版本是|The current OS version is)(.*)(.|.)";

    /**
     * APPLICATION_NAMES
     */
    public static final String APPLICATION_NAMES = "^application_names=(.+)";

    /**
     * START_APP_COMMANDS
     */
    public static final String START_APP_COMMANDS = "^start_app_commands=(.+)";

    /**
     * STOP_APP_COMMANDS
     */
    public static final String STOP_APP_COMMANDS = "^stop_app_commands=(.+)";

    /**
     * CLAM_RESULT_PATTERN
     */
    public static final String CLAM_RESULT_PATTERN = "Infected files: (.+)";

    /**
     * 功能测试内存 SHELL_UNIT_RUN_PATTERN
     */
    public static final String SHELL_UNIT_RUN_PATTERN = "^Ran \\\u001B\\[1;36m(.*)\\\u001B\\[0m test";

    /**
     * 功能测试内存 SHELL_UNIT_TEST_PATTERN
     */
    public static final String SHELL_UNIT_TEST_PATTERN = "^-+ Test file name : (.*).sh -+";

    /**
     * 功能测试内存 SHELL_UNIT_FAILED_PATTERN
     */
    public static final String SHELL_UNIT_FAILED_PATTERN = "^\\\u001B\\[1;31mFAILED\\\u001B\\[0m \\(\\\u001B\\[1;"
            + "31mfailures=(.*)\\\u001B\\[0m\\)";

    /**
     * PYTEST_SUCCESS_PATTERN
     */
    public static final String PYTEST_SUCCESS_PATTERN = "^=+ (.*) passed in .* =+";

    /**
     * PYTEST_RUN_PATTERN
     */
    public static final String PYTEST_RUN_PATTERN = "^-+ Test file name : (.*).py -+";

    /**
     * PYTEST_FAILED_PATTERN
     */
    public static final String PYTEST_FAILED_PATTERN = "^=+ (.*) failed, (.*) passed in .*=+";

    /**
     * APP_PACKAGE_TEST_TOPIC
     */
    public static final String APP_PACKAGE_TEST_TOPIC = "App_Package_Test";

    /**
     * SECURITY_SCAN_TAG
     */
    public static final String SECURITY_SCAN_TAG = "Security_Scan";

    /**
     * INFO_TEST_CASE_RESULT_MAP
     */
    public static final Map<String, InfoTestCaseResult> INFO_TEST_CASE_RESULT_MAP =
            Collections.unmodifiableMap(new HashMap<String, InfoTestCaseResult>() {
                {
                    put("FAILED_TO_START_APP", new InfoTestCaseResult(
                            TEST_FAILED, "软件启动失败.", "Failed to start the software."));
                    put("SUCCESS_TO_START_APP", new InfoTestCaseResult(
                            TEST_PASSED, "", ""));
                    put("FAILED_TO_RELIABLE_TEST", new InfoTestCaseResult(
                            TEST_FAILED, "强制杀死进程,启动失败,异常测试失败.",
                            "After the process was forcibly killed,"
                                    + " the process failed to be restarted, and exception test failed."));
                    put("SUCCESS_TO_RELIABLE_TEST", new InfoTestCaseResult(
                            TEST_PASSED, "", ""));
                    put("FAILED_TO_STOP_APP", new InfoTestCaseResult(
                            TEST_FAILED, "软件停止失败.", "Failed to stop the software."));
                    put("SUCCESS_TO_STOP_APP", new InfoTestCaseResult(
                            TEST_PASSED, "", ""));
                    put("FILE_NOT_EXIT", new InfoTestCaseResult(
                            TEST_FAILED, "测试结果中没有兼容性测试工具的日志文件.",
                            "The compressed file package does not contain"
                                    + " the log file of the compatibility test tool."));
                    put("NO_START_APP", new InfoTestCaseResult(TEST_FAILED, "没有执行软件启动.",
                            "You have no started the software."));
                    put("NO_STOP_APP", new InfoTestCaseResult(TEST_FAILED, "没有执行软件停止.",
                            "You have no stop the software."));
                    put("NO_RELIABLE_TEST", new InfoTestCaseResult(TEST_FAILED, "没有执行可靠性测试.",
                            "You have no conducted the reliability test."));
                    put("UNZIP_ERROR", new InfoTestCaseResult(TEST_FAILED, "测试结果解压错误.",
                            "An error occurred when decompressing the log file "
                                    + "of the test tool from the uploaded file package."));
                    put("READ_ERROR", new InfoTestCaseResult(TEST_FAILED, "测试结果读取错误.",
                            "An error occurred when reading the log file"
                                    + " of the test tool from the uploaded file package."));
                }
            });

    /**
     * SOFTWARE_COMPARE_DESC_LIST
     */
    public static final List<String> SOFTWARE_COMPARE_DESC_LIST = Collections.unmodifiableList(
            Arrays.asList("未匹配到相应软件：%s", "兼容性测试配置文件填写的应用软件为：%s",
                    "兼容性测试配置文件应用软件名称为空", "没有找到进程快照文件", "解析配置文件和软件进程栈信息出错"));

    /**
     * SOFTWARE_COMPARE_DESC_EN_LIST
     */
    public static final List<String> SOFTWARE_COMPARE_DESC_EN_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    "No matching software %s found.", "The application software recorded in the compatibility test "
                            + "configuration file is %s",
                    "The name of the application software in the compatibility test configuration file is %s.",
                    "Cannot find the process snapshot file in the uploaded file package.",
                    "An error occurred when reading the process snapshot file in the uploaded file package."));

    /**
     * COMPATIBILITY_DESC_LIST
     */
    public static final List<String> COMPATIBILITY_DESC_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    "被测试软件启动前采集%1$s资源利用率与被测试软件停止后%1$s利用率之间的波动为%2$,.2f %%.",
                    "被测试软件启动前采集%1$s资源利用率与被测试软件停止后%1$s利用率之间的波动为%2$,.2f %%,%3$s 1.00 %% .",
                    "被测试软件启动前采集%1$s资源利用率与被测试软件停止后%1$s资源利用率文件解析出错.",
                    "被测试软件启动前采集%1$s资源利用率与被测试软件停止后%1$s资源利用率文件缺失.",
                    "根据端口扫描的结果,结合软件端口矩阵,合适是否存在未知端口,并澄清原因.", "%s协议扫描结果如下：",
                    "被测试软件启动前采集的网卡资源%1$s数据与被测软件停止后采集网卡资源%1$s数据之间的波动为%2$,"
                            + ".2f %%,%s测试前波动的1.00%%.",
                    "被测试软件启动前采集的网卡资源%1$s数据与被测软件停止后采集网卡资源%1$s数据之间的波动为%2$,.2f %%."));

    /**
     * COMPATIBILITY_DESC_EN_LIST
     */
    public static final List<String> COMPATIBILITY_DESC_EN_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    "The change rate of the %s usage collected before the tested software was started "
                            + "and after the target software was stopped is %.2f %%.",
                    "The change rate of the %s usage collected before the tested software was started "
                            + "and after the target software was stopped is %.2f %%, which is %s than 1.00%%.",
                    "An error occurred when parsing the resource files related to %s,"
                            + "before the tested software was started and after the software was stopped.",
                    "An error occurred when reading the resource files related to %s,"
                            + "before the tested software was started and after the software was stopped.",
                    "Check whether unknown ports are opened according to the port scanning "
                            + "result and the tested software port matrix, and locate the cause.",
                    "The scanning result of the %s protocols is as follows:",
                    "The data %s by the NIC fluctuates by %.2f %% before the tested software was "
                            + "started and after the tested software was stopped,"
                            + " which is%s than 1.00%% before the test.",
                    "The data %s by the NIC fluctuates by %.2f %% before the tested software was "
                            + "started and after the software was stopped."
            ));

    /**
     * PERFORMANCE_DESC_LIST
     */
    public static final List<String> PERFORMANCE_DESC_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    "压力测试期间%s 资源波动值为%.2f %%,%s 5.00%%.", "后续可结合实际业务澄清波动合理性",
                    "压力测试期间%s 资源文件读取出错.", "压力测试期间%s 资源文件缺失.",
                    "压力测试期间网卡%s数据波动是%.2f %%.", "压力测试期间网卡%s数据波动是%.2f %%,%s 5.00%%."
            ));

    /**
     * PERFORMANCE_DESC_EN_LIST
     */
    public static final List<String> PERFORMANCE_DESC_EN_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    "During the pressure test, the fluctuation value of %s is %.2f %%, which is %s than 5.00%%.",
                    "The cause of the fluctuation can be clarified based on actual services.",
                    "An error occurred when parsing the %s resource file during the pressure test.",
                    "The %s resource file is missing during the pressure test.",
                    "During the pressure test, the fluctuation value of data %s by the NIC is %.2f %%.",
                    "During the pressure test, the fluctuation value of data %s"
                            + " by the NIC is %.2f %%, which is %s than 5.00%%."
            ));

    /**
     * LOG_WHITE_RULES
     */
    public static final Map<String, Pattern> LOG_WHITE_RULES = Collections.unmodifiableMap(
            new HashMap<String, Pattern>() {
                {
                    put("FAILED_TO_START_APP", Pattern.compile(
                            "#\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}#.*(#5)?#"
                                    + "(检查到业务应用启动不成功|"
                                    + "Failed to start the application (.*). Check the startup script.)"));
                    put("SUCCESS_TO_START_APP", Pattern.compile(
                            "#\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}#.*(#5)?#(业务应用("
                                    + ".*)(启动)?完成|" + "Succeeded in starting the application (.*))"));
                    put("FAILED_TO_RELIABLE_TEST", Pattern.compile(
                            "#\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}#.*(#8)?#"
                                    + "(可靠性测试, 业务应用(.*)启动失败,可靠性测试失败|"
                                    + "可靠性测试, 执行强制杀死进程(.*)报错, 可靠性测试失败|"
                                    + "可靠性测试前, 业务应用(.*)已停止, 可靠性测试失败|"
                                    + "(.*)The reliability test failed.)"));
                    put("SUCCESS_TO_RELIABLE_TEST", Pattern.compile(
                            "#\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}#.*(#8)?#"
                                    + "(可靠性测试, 业务应用(.*)启动完成.可靠性测试成功|"
                                    + "(.*)The reliability test is successful.)"));
                    put("FAILED_TO_STOP_APP", Pattern.compile(
                            "#\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}#.*(#9)?#"
                                    + "(检查到应用程序还在启动,且用户未能停止应用|"
                                    + "Failed to stop the service application (.*). Check the stop script.)"));
                    put("SUCCESS_TO_STOP_APP", Pattern.compile(
                            "#\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}#.*(#9)?#(进程(.*)"
                                    + "不存在|The (.*) process does not exist.)"));
                }
            });

    /**
     * CONNECT_EXCEPTION_DESC_MAP
     */
    public static final Map<String, String> CONNECT_EXCEPTION_DESC_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", CONNECT_EXCEPTION_DESC);
                    put("EN", "Connection exception. Check whether the login account and password are correct.");
                }
            });

    /**
     * TASK_DUPLICATE_MAP
     */
    public static final Map<String, String> TASK_DUPLICATE_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", TASK_DUPLICATE);
                    put("EN", "The test is proceeding in %s. Please do not submit repeatedly.");
                }
            });

    /**
     * TASK_DUPLICATE_MAP
     */
    public static final Map<String, String> TAR_FILE_TOO_BIG_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", TAR_FILE_TOO_BIG);
                    put("EN", "The uploaded file cannot exceed 100MB after decompression. "
                        + "Modify the file and upload it again.");
                }
            });

    /**
     * FAILED_ENCRYPT_MAP
     */
    public static final Map<String, String> FAILED_ENCRYPT_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", "加密参数失败.");
                    put("EN", "Failed to encrypt the parameter.");
                }
            });

    /**
     * FAILED_ENCRYPT_MAP
     */
    public static final Map<String, String> TEMPLATE_DECOMPRESSION_ERROR_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", TEMPLATE_DECOMPRESSION_ERROR);
                    put("EN", "Download the template, modify the file according to the template, "
                        + "and upload the compressed file after modification.");
                }
            });

    /**
     * FILE_FAILED_VIRUS_SCAN_MAP
     */
    public static final Map<String, String> FILE_FAILED_VIRUS_SCAN_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", FILE_FAILED_VIRUS_SCAN);
                    put("EN", "The file failed the antivirus scan. Modify the file and upload it again.");
                }
            });

    /**
     * FILE_TOO_BIG_MAP
     */
    public static final Map<String, String> FILE_TOO_BIG_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", FILE_TOO_BIG);
                    put("EN", "The uploaded file cannot exceed 100MB after decompression.");
                }
            });

    /**
     * FILE_TYPE_ERROR_MAP
     */
    public static final Map<String, String> FILE_TYPE_ERROR_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", "文件解析出错.");
                    put("EN", "An error occurred when parsing the file.");
                }
            });

    /**
     * UPLOAD_FILE_STATUS_NORMAL_MAP
     */
    public static final Map<String, String> UPLOAD_FILE_STATUS_NORMAL_MAP = Collections.unmodifiableMap(
            new HashMap<String, String>() {
                {
                    put("CN", "00");
                    put("EN", "00");
                }
            });

    /**
     * DEPENDENCY_INSTALL_FAILURE
     */
    public static final Map<String, String> DEPENDENCY_INSTALL_FAILURE =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", "依赖软件安装异常.");
                    put("EN", "Dependency installation exception.");
                }
            });

    /**
     * APP_START_UP_FAILURE_DESC_MAP
     */
    public static final Map<String, String> APP_START_UP_FAILURE_DESC_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", "待测试应用软件启动失败.");
                    put("EN", "Failed to start the application to be tested.");
                }
            });

    /**
     * APP_STOP_FAILURE_DESC_MAP
     */
    public static final Map<String, String> APP_STOP_FAILURE_DESC_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", "待测试应用软件停止失败.");
                    put("EN", "Failed to stop the application to be tested.");
                }
            });

    /**
     * PERFORMANCE_FILE_MISSING_MAP
     */
    public static final Map<String, String> PERFORMANCE_FILE_MISSING_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", "没有上传性能测试文件");
                    put("EN", "No performance test file is uploaded.");
                }
            });

    /**
     * TASK_EXPIRE_ESC_MAP
     */
    public static final Map<String, String> TASK_EXPIRE_ESC_MAP =
            Collections.unmodifiableMap(new HashMap<String, String>() {
                {
                    put("CN", "测试任务超时");
                    put("EN", "The test task timed out without feedback.");
                }
            });

    /**
     * InfoTestCaseResult
     *
     * @author kongcaizhi
     * @since 2021-10-19
     */
    @AllArgsConstructor
    @Data
    public static final class InfoTestCaseResult {
        private final String testResult;
        private final String testReason;
        private final String testReasonEn;
    }

    /**
     * CompatibilityTarFile
     *
     * @author kongcaizhi
     * @since 2021-10-19
     */
    public enum CompatibilityTarFile {
        HARDWARE_INFO("data/hardware/hardware_info.log", 0),
        INFO_LOG("data/others/info.log", 1),
        CONFIGURE_INFO("data/compatibility_testing.conf", 2),
        PRODUCT_NAME("data/product/product_name.log", 3),
        COMPATIBILITY_CPU_0("data/test/compatiable/test_perf_cpu_0.log", 4),
        COMPATIBILITY_CPU_1("data/test/compatiable/test_perf_cpu_1.log", 5),
        COMPATIBILITY_MEM_0("data/test/compatiable/test_perf_mem_0.log", 6),
        COMPATIBILITY_MEM_1("data/test/compatiable/test_perf_mem_1.log", 7),
        COMPATIBILITY_DISK_0("data/test/compatiable/test_perf_disk_0.log", 8),
        COMPATIBILITY_DISK_1("data/test/compatiable/test_perf_disk_1.log", 9),
        COMPATIBILITY_NET_0("data/test/compatiable/test_perf_net_0.log", 10),
        COMPATIBILITY_NET_1("data/test/compatiable/test_perf_net_1.log", 11),
        PERFORMANCE_CPU("data/test/performance/test_perf_cpu_1.log", 12),
        PERFORMANCE_MEM("data/test/performance/test_perf_mem_1.log", 13),
        PERFORMANCE_DISK("data/test/performance/test_perf_disk_1.log", 14),
        PERFORMANCE_NET("data/test/performance/test_perf_net_1.log", 15),
        CLAM_LOG("data/test/safety/clam.log", 16),
        FUNCTION_SHELL_UNIT("data/test/function/bash_test.log", 17),
        FUNCTION_PY_TEST("data/test/function/python_test.log", 18),
        CVE_CHECK_LOG("data/test/safety/cvecheck-result.json", 19);

        private final String fileName;

        CompatibilityTarFile(String fileName, int fileIndex) {
            this.fileName = fileName;
        }

        /**
         * get File Name
         *
         * @return File Name
         */
        public String getFileName() {
            return this.fileName;
        }
    }

    /**
     * CompatibilityTestName
     *
     * @author kongcaizhi
     * @since 2021-10-19
     */
    public enum CompatibilityTestName {
        HARDWARE_SERVER("Compatibility_Hardware_Server", 0),
        SOFTWARE_NAME("Compatibility_Software_Name", 1),
        APPLICATION_START("Compatibility_Application_Start", 2),
        APPLICATION_STOP("Compatibility_Application_Stop", 3),
        IDLE_CPU("Compatibility_Idle_Cpu", 4),
        IDLE_MEM("Compatibility_Idle_Memory", 5),
        IDLE_DISK("Compatibility_Idle_Disk", 6),
        IDLE_NET("Compatibility_Idle_Network", 7),
        PRESSURE_CPU("Reliability_Pressure_Cpu", 8),
        PRESSURE_MEM("Reliability_Pressure_Memory", 9),
        PRESSURE_DISK("Reliability_Pressure_Disk", 10),
        PRESSURE_NET("Reliability_Pressure_Network", 11),
        EXCEPTION_KILL("Reliability_Exception_Kill", 12),
        SECURITY_PORT("Security_Base_Port", 13),
        SECURITY_VIRUS("Security_Base_Virus", 14),
        SECURITY_VULNERABLE("Security_Base_CveCheck", 15),
        FUNCTION_TEST("Function_Test", 16);

        private final String testName;

        CompatibilityTestName(String testName, int testIndex) {
            this.testName = testName;
        }

        /**
         * get Test Name
         *
         * @return Test Name
         */
        public String getTestName() {
            return this.testName;
        }
    }

    /**
     * CompatibilityTestStep
     *
     * @author kongcaizhi
     * @since 2021-10-19
     */
    public enum CompatibilityTestStep {
        DEPENDENCY_INSTALL_SUCCESS("0210", "依赖软件安装完成"),
        DEPENDENCY_INSTALL_FAILURE("0211", "依赖软件安装失败"),
        APP_START_SUCCESS("0310", "应用软件启动完成"),
        APP_START_FAILURE("0311", "应用软件启动失败"),
        APP_STOP_SUCCESS("0320", "应用软件停止完成"),
        APP_STOP_FAILURE("0321", "应用软件停止失败"),
        COMPATIBILITY_TEST_SUCCESS("0410", "兼容性测试中"),
        COM_START_SUCCESS("0510", "应用软件启动完成"),
        COM_START_FAILURE("0511", "应用软件启动失败"),
        PORT_TEST_SUCCESS("0610", "端口扫描完成"),
        VIRUS_SCAN_SUCCESS("0620", "病毒扫描完成"),
        VULNERABLE_SCAN_SUCCESS("0630", "漏洞扫描完成"),
        EXCEPTION_TEST_FINISH("0810", "可靠性测试完成"),
        COMPATIBILITY_TEST_FINISH("0910", "兼容性测试结束"),
        FUN_TEST_FILE_EXIST_ERROR("1011", "测试失败-功能测试文件不存在"),
        FUN_TEST_FILE_DIR_ERROR("1012", "测试失败-功能测试文件function_testing目录不存在"),
        FUN_TEST_PYTEST_ERROR("1013", "测试失败-pytest安装失败");

        private final String stepIndex;

        CompatibilityTestStep(String stepIndex, String stepDesc) {
            this.stepIndex = stepIndex;
        }

        /**
         * get Step Index
         *
         * @return stepIndex
         */
        public String getStepIndex() {
            return this.stepIndex;
        }

        /**
         * fromStepIndex
         *
         * @param stepIndex stepIndex
         * @return CompatibilityTestStep
         */
        public static CompatibilityTestStep fromStepIndex(String stepIndex) {
            CompatibilityTestStep compatibilityTestStep = null;
            for (CompatibilityTestStep step : CompatibilityTestStep.values()) {
                if (step.getStepIndex().equals(stepIndex)) {
                    compatibilityTestStep = step;
                    break;
                }
            }
            return compatibilityTestStep;
        }
    }
}
