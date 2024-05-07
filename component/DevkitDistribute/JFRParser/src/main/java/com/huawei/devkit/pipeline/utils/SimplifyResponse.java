package com.huawei.devkit.pipeline.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

public class SimplifyResponse {
    private static final Logger logger = LogManager.getLogger(SimplifyResponse.class);

    public <T> Map<String, List<Object>> simplify(List<T> before, Class<T> clazz) {
        Map<String, List<Object>> result = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true); // 允许访问私有字段
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            if (Objects.isNull(annotation)) {
                continue;
            }
            String value = annotation.value();
            List<Object> list = new ArrayList<>();
            result.put(value, list);
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
