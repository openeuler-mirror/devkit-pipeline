/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.Builder;
import lombok.Data;

/**
 * NormalResp
 *
 * @author kongcaizhi
 * @since 2021-03-23
 */
@Data
@Builder
public class NormalResp {
    private String code;
    private String msg;
    private String data;

    /**
     * toJSONString
     *
     * @return String
     */
    public String toJSONString() {
        return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue);
    }

    /**
     * ok
     *
     * @return String
     */
    public static String ok() {
        return NormalResp.builder().code("0000").msg("Success").build().toJSONString();
    }

    /**
     * ok
     *
     * @param data data
     * @return String
     */
    public static String ok(String data) {
        return NormalResp.builder().code("0000").data(data).msg("Success").build().toJSONString();
    }

    /**
     * badRequest
     *
     * @param data data
     * @return String
     */
    public static String badRequest(String data) {
        return NormalResp.builder().code("4001").data(data).msg("Bad Request").build().toJSONString();
    }
}
