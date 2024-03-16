/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import com.huawei.ic.openlab.cloudtest.util.Constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MQSMessage
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Data
public class MqsMessage {
    private String projectId;
    private String userId;
    private String serverIp;
    private String status;
    private String statusDesc;
    private String statusTime;
    private MessageDetail detail;
    private MessageResult testResult;

    /**
     * MessageDetail
     *
     * @author kongcaizhi
     * @since 2021-10-30
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class MessageDetail {
        private String step;
        private String stepStatus;
        private String exceptionType;
        private String exceptionDesc;
        private String exceptionFileName;
        private String resultFileName;
        private String fileStatus;

        /**
         * DEPENDENCY_INSTALL_SUCCESS
         */
        public void setDependencyInstallSuccess() {
            this.setStep(Constants.DEPENDENCY_INSTALL_DESC);
            this.setStepStatus(Constants.TEST_FINISHED_CN);
        }

        /**
         * APP_START_SUCCESS
         */
        public void setAppStartSuccess() {
            this.setStep(Constants.DEPENDENCY_INSTALL_DESC);
            this.setStepStatus(Constants.TEST_FINISHED_CN);
        }

        /**
         * APP_STOP_SUCCESS
         *
         * @param isCompatibilityTest isCompatibilityTest
         * @param isReliabilityTest isReliabilityTest
         * @param isSecurityTest isSecurityTest
         */
        public void setAppStopSuccess(boolean isCompatibilityTest, boolean isReliabilityTest, boolean isSecurityTest) {
            this.setStep(Constants.APP_STOP_DESC);
            this.setStepStatus(Constants.TEST_FINISHED_CN);
            if (isCompatibilityTest) {
                this.setStep(Constants.COMPATIBILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
            if (isReliabilityTest) {
                this.setStep(Constants.RELIABILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
            if (isSecurityTest) {
                this.setStep(Constants.SECURITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
        }

        /**
         * COM_START_SUCCESS
         *
         * @param isReliabilityTest isReliabilityTest
         * @param isSecurityTest isSecurityTest
         */
        public void setComStartSuccess(boolean isReliabilityTest, boolean isSecurityTest) {
            this.setStep(Constants.COMPATIBILITY_TEST_DESC);
            this.setStepStatus(Constants.TEST_PROCESSING);
            if (isReliabilityTest) {
                this.setStep(Constants.RELIABILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
            if (isSecurityTest) {
                this.setStep(Constants.SECURITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
        }

        /**
         * PORT_TEST_SUCCESS
         */
        public void setPortTestSuccess() {
            this.setStep(Constants.DEPENDENCY_INSTALL_DESC);
            this.setStepStatus(Constants.TEST_FINISHED_CN);
        }

        /**
         * VIRUS_SCAN_SUCCESS
         *
         * @param isCompatibilityTest isCompatibilityTest
         * @param isReliabilityTest isReliabilityTest
         */
        public void setVirusScanSuccess(boolean isCompatibilityTest, boolean isReliabilityTest) {
            this.setStep(Constants.SECURITY_TEST_DESC);
            this.setStepStatus(Constants.TEST_PROCESSING);
            if (isCompatibilityTest) {
                this.setStep(Constants.COMPATIBILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
            if (isReliabilityTest) {
                this.setStep(Constants.RELIABILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
        }

        /**
         * VULNERABLE_SCAN_SUCCESS
         *
         * @param isCompatibilityTest isCompatibilityTest
         * @param isReliabilityTest isReliabilityTest
         */
        public void setVulnerableScanSuccess(boolean isCompatibilityTest, boolean isReliabilityTest) {
            this.setStep(Constants.SECURITY_TEST_DESC);
            this.setStepStatus(Constants.TEST_FINISHED_CN);
            if (isCompatibilityTest) {
                this.setStep(Constants.COMPATIBILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
            if (isReliabilityTest) {
                this.setStep(Constants.RELIABILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
        }

        /**
         * EXCEPTION_TEST_FINISH
         *
         * @param isCompatibilityTest isCompatibilityTest
         */
        public void setExceptionTestFinish(boolean isCompatibilityTest) {
            this.setStep(Constants.RELIABILITY_TEST_DESC);
            this.setStepStatus(Constants.TEST_FINISHED_CN);
            if (isCompatibilityTest) {
                this.setStep(Constants.COMPATIBILITY_TEST_DESC);
                this.setStepStatus(Constants.TEST_PROCESSING);
            }
        }
    }

    /**
     * MessageResult
     *
     * @author kongcaizhi
     * @since 2021-10-30
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class MessageResult {
        private CloudLabTestTask.TestCaseSummary testSummary;
        private List<TestCaseResult> testDetail;
        private ScriptResultConfig scriptResultConfig;
    }
}
