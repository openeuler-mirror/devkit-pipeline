/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.perload;

import com.huawei.devkit.code.inspector.dao.DataBaseMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * DataBasePreLoad
 *
 * @since 2024-07-11
 */
public class DataBasePreLoad {
    public static SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

    public static void preload(Properties properties) throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);
        }
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            createTable(sqlSession);
        }
    }

    private static void createTable(SqlSession sqlSession) {
        DataBaseMapper mapper = sqlSession.getMapper(DataBaseMapper.class);
        mapper.createTable();
        sqlSession.commit();
    }


    private static SqlSessionFactory sqlSessionFactory;
}
