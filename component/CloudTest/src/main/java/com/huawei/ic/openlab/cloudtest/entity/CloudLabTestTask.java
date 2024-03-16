/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 远程实验室测试实体类
 *
 * @author kongcaizhi
 * @since 2021-11-11
 */
@Data
public class CloudLabTestTask {
    private String projectId;
    private String userId;
    private String serverIp;
    private int serverPort;
    private String serverUser;
    private String serverPassword;

    private int projectStatus = 0;
    private String statusDesc;

    private String requestTime;
    private String startTime;
    private String finishTime;

    @JsonProperty(value = "compatibilityTest")
    private boolean isCompatibilityTest;
    @JsonProperty(value = "reliabilityTest")
    private boolean isReliabilityTest;
    @JsonProperty(value = "securityTest")
    private boolean isSecurityTest;
    @JsonProperty(value = "functionTest")
    private boolean isFunctionTest;
    @JsonProperty(value = "performanceTest")
    private boolean isPerformanceTest;

    private StepStatus stepStatus;
    private String currentStatus;
    private UploadFile functionFile;
    private UploadFile performanceFile;
    private UploadFile resultFile;
    private TestCaseSummary testSummary;
    private List<TestCaseResult> testDetail;
    private ScriptResultConfig scriptResultConfig;
    private String performanceService;
    private TestBeginTime testBeginTime;
    private String taskLanguage;

    /**
     * StepStatus
     *
     * @author kongcaizhi
     * @since 2021-11-11
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class StepStatus {
        private String step1Code;
        private String step1Time;
        private UploadFile step1File;
        private String step2Code;
        private String step2Time;
        private UploadFile step2File;
        private String step3Code;
        private String step3Time;
        private UploadFile step3File;
        private String step4Code;
        private String step4Time;
        private UploadFile step4File;
        private String step5Code;
        private String step5Time;
        private UploadFile step5File;
        private String step6Code;
        private String step6Time;
        private UploadFile step6File;
        private String step7Code;
        private String step7Time;
        private UploadFile step7File;
        private String step8Code;
        private String step8Time;
        private UploadFile step8File;
        private String step9Code;
        private String step9Time;
        private UploadFile step9File;
        private String step10Code;
        private String step10Time;
        private UploadFile step10File;
        private String functionCode;
        private String functionTime;
        private UploadFile functionFile;
        private String performanceCode;
        private String performanceTime;
        private UploadFile performanceFile;

        /**
         * set Step1
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep1(String resultCode, String timeString) {
            this.setStep1Code(resultCode);
            this.setStep1Time(timeString);
        }

        /**
         * set Step2
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep2(String resultCode, String timeString) {
            this.setStep2Code(resultCode);
            this.setStep2Time(timeString);
        }

        /**
         * set Step3
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep3(String resultCode, String timeString) {
            if (StringUtils.isNoneEmpty(this.getStep3Code())) {
                this.setStep3Code(this.getStep3Code() + ";" + resultCode);
            } else {
                this.setStep3Code(resultCode);
            }
            this.setStep3Time(timeString);
        }

        /**
         * set Step4
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep4(String resultCode, String timeString) {
            this.setStep4Code(resultCode);
            this.setStep4Time(timeString);
        }

        /**
         * set Step5
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep5(String resultCode, String timeString) {
            this.setStep5Code(resultCode);
            this.setStep5Time(timeString);
        }

        /**
         * set Step6
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep6(String resultCode, String timeString) {
            this.setStep6Code(resultCode);
            this.setStep6Time(timeString);
        }

        /**
         * set Step7
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep7(String resultCode, String timeString) {
            this.setStep7Code(resultCode);
            this.setStep7Time(timeString);
        }

        /**
         * set Step8
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep8(String resultCode, String timeString) {
            this.setStep8Code(resultCode);
            this.setStep8Time(timeString);
        }

        /**
         * set Step9
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep9(String resultCode, String timeString) {
            this.setStep9Code(resultCode);
            this.setStep9Time(timeString);
        }

        /**
         * set Step10
         *
         * @param resultCode resultCode
         * @param timeString timeString
         */
        public void setStep10(String resultCode, String timeString) {
            this.setStep10Code(resultCode);
            this.setStep10Time(timeString);
        }
    }

    /**
     * UploadFile
     *
     * @author kongcaizhi
     * @since 2021-11-11
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class UploadFile {
        private String fileDesc;
        private String fileStatus;
        private String fileId;
        private String fileName;
        private String filePath;
    }

    /**
     * TestBeginTime
     *
     * @author kongcaizhi
     * @since 2021-11-11
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class TestBeginTime {
        private String compatibilityTime;
        private String reliabilityTime;
        private String securityTime;
        private String functionTime;
        private String performanceTime;
    }

    /**
     * TestCaseSummary
     *
     * @author kongcaizhi
     * @since 2021-11-11
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class TestCaseSummary {
        private TestCaseResultCount function;
        private TestCaseResultCount performance;
        private TestCaseResultCount security;
        private TestCaseResultCount compatibility;
        private TestCaseResultCount reliability;

        public TestCaseSummary(TestCaseResultCount function, TestCaseResultCount security,
                               TestCaseResultCount compatibility, TestCaseResultCount reliability) {
            this.function = function;
            this.security = security;
            this.compatibility = compatibility;
            this.reliability = reliability;
        }
    }
}
