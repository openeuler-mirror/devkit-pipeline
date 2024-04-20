package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.FlameItem;

import java.util.Map;
import java.util.Objects;

public class FlameUtils {

    public static void mergeSubFlame(Map<String, FlameItem> subMap, Map<String, FlameItem> externalSubMap) {
        if (Objects.isNull(externalSubMap)) {
            return;
        }
        for (Map.Entry<String, FlameItem> entry : externalSubMap.entrySet()) {
            String key = entry.getKey();
            FlameItem origin = subMap.get(key);
            FlameItem external = entry.getValue();
            if (Objects.isNull(origin)) {
                subMap.put(key, external);
            } else {
                origin.setValue(origin.getValue() + external.getValue());
                mergeSubFlame(origin.getSubMap(), external.getSubMap());
            }
        }
    }
}
