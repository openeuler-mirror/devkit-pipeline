/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.common.typehandler;

import com.huawei.ic.openlab.cloudtest.common.exception.BaseException;
import com.huawei.ic.openlab.cloudtest.util.AESUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * CryptoTypeHandler
 *
 * @author kongcaizhi
 * @since 2021-11-19
 */
@Slf4j
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CryptoTypeHandler extends BaseTypeHandler<String> {
    private static final Integer PASSWORD_LENGTH_WITHOUT_ENCRYPT = 50;

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, String s, JdbcType jdbcType)
            throws SQLException {
        preparedStatement.setString(i, doEncrypt(s));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return doDecruypt(resultSet.getString(s));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return doDecruypt(resultSet.getString(i));
    }

    @Override
    public String getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return doDecruypt(callableStatement.getString(i));
    }

    private String doEncrypt(String password) {
        return AESUtil.gcmEncrypt(password, AESUtil.CRYPTO.getAesDefaultKey(), AESUtil.CRYPTO.getAesDefaultAad());
    }

    private String doDecruypt(String password) {
        if (!isEncrypt(password)) {
            throw new BaseException("password decruypt failed");
        }
        return AESUtil.gcmDecrypt(password, AESUtil.CRYPTO.getAesDefaultKey());
    }

    private boolean isEncrypt(String password) {
        return (StringUtils.isNotEmpty(password) && password.length() > PASSWORD_LENGTH_WITHOUT_ENCRYPT)
                ? true : false;
    }
}
