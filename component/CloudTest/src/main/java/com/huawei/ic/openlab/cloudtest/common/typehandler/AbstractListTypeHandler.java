/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.common.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.json.JsonSanitizer;

import lombok.SneakyThrows;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * AbstractListTypeHandler
 *
 * @author kongcaizhi
 * @since 2021-11-19
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public abstract class AbstractListTypeHandler extends BaseTypeHandler<List> {
    /**
     * objectMapper
     */
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * javaType
     */
    protected CollectionType javaType;

    @SneakyThrows
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List list, JdbcType jdbcType) throws SQLException {
        ps.setString(i, objectMapper.writeValueAsString(list));
    }

    @SneakyThrows
    @Override
    public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        List<?> list = null;
        if (!StringUtils.hasLength(json)) {
            list = objectMapper.readValue(JsonSanitizer.sanitize(json), javaType);
        }
        return list;
    }

    @SneakyThrows
    @Override
    public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(rs.getString(columnIndex));
        List<?> list = null;
        if (!StringUtils.hasLength(json)) {
            list = objectMapper.readValue(JsonSanitizer.sanitize(json), javaType);
        }
        return list;
    }

    @SneakyThrows
    @Override
    public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        List<?> list = null;
        if (!StringUtils.hasLength(json)) {
            list = objectMapper.readValue(JsonSanitizer.sanitize(json), javaType);
        }
        return list;
    }
}
