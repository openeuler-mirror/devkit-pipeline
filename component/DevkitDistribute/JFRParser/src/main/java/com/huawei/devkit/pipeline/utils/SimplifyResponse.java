package com.huawei.devkit.pipeline.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimplifyResponse {
    private static final Logger logger = LogManager.getLogger(SimplifyResponse.class);

    public static <T> Map<String, List<Object>> simplify(List<T> before, Class<T> clazz) {
        Map<String, List<Object>> result = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 允许访问私有字段
            JsonIgnore ignore = field.getAnnotation(JsonIgnore.class);
            if (Objects.nonNull(ignore)) {
                continue;
            }
            List<Object> list = new ArrayList<>();
            result.put(field.getName(), list);
            try {
                for (T item : before) {
                    list.add(field.get(item));
                }
            } catch (IllegalAccessException e) {
                logger.error(e);
            }
        }

        return result;
    }
}
