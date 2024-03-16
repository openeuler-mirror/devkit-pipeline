/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import com.huawei.ic.openlab.cloudtest.util.Constants;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import lombok.Data;

import org.springframework.util.StringUtils;

import java.util.List;

/**
 * TaskDelay
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@Data
public class TestCaseResult {
    private String id;
    private String result;
    private String evidence;
    private String reason;

    /**
     * transferResult
     *
     * @return String
     */
    public String transferResult() {
        if (Constants.TEST_PASSED.equals(result)) {
            return "通过";
        } else {
            return "不通过";
        }
    }

    /**
     * transferEvidence
     *
     * @return String
     */
    public String transferEvidence() {
        JSONArray array = JSON.parseArray(evidence);
        List<String> list = array.toJavaList(String.class);
        return String.join("<w:br/>", list);
    }

    /**
     * transferReason
     *
     * @return String
     */
    public String transferReason() {
        if (StringUtils.hasLength(reason)) {
            return ",原因为：" + reason;
        } else {
            return "";
        }
    }
}
