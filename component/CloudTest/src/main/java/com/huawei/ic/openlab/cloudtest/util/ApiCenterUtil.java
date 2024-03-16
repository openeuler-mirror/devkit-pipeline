/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import com.huawei.ic.openlab.cloudtest.entity.SystemParams;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.SSLContext;

/**
 * API网关的接口工具类
 *
 * @author kongcaizhi
 * @since 2022-10-24
 */
@Slf4j
public class ApiCenterUtil {
    private static final String VALIDATED_TOKEN_SUCCESS_CODE = "200";

    private static HttpHeaders getBasicHttpHeaders(SystemParams systemParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add("X-HW-ID", systemParams.getAppId());
        headers.add("X-HW-APPKEY", systemParams.getAppKey());
        return headers;
    }

    private static RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);
    }

    /**
     * 向API网关验证token是否有效
     *
     * @param systemParams 系统参数
     * @param token token
     * @param taskDeviceId taskDeviceId
     * @return boolean 是否有效
     */
    public static boolean validateToken(SystemParams systemParams, String token, String taskDeviceId) {
        // 添加请求头
        HttpEntity request = new HttpEntity<>(null, getBasicHttpHeaders(systemParams));

        // 发送GET请求
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(systemParams.getKunpengServiceUrl())
                    .queryParam("sessionId", token)
                    .queryParam("token", taskDeviceId)
                    .queryParam("tag", 1);
            String requestUrl = builder.build().encode().toUriString();
            ResponseEntity<ValidatedResp> response = restTemplate().exchange(requestUrl, HttpMethod.GET, request,
                    ValidatedResp.class);

            log.info("request apiwg check token return{}", JSONObject.toJSONString(response.getBody()));
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()
                    && VALIDATED_TOKEN_SUCCESS_CODE.equals(Objects.requireNonNull(response.getBody()).getCode())) {
                return true;
            } else {
                return false;
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | RestClientException ex) {
            log.error("request apiwg check taskDeviceId {} exception {}", taskDeviceId,
                    ex.getLocalizedMessage());
            return false;
        }
    }

    @Data
    private static class ValidatedResp {
        private String code;
        private String msg;
        private String data;
        @JsonProperty("success")
        private Boolean isSuccess;
    }
}
