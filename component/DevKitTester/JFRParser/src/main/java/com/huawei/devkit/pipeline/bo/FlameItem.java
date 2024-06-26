package com.huawei.devkit.pipeline.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.devkit.pipeline.utils.JfrMethodSignatureParser;
import jdk.jfr.consumer.RecordedFrame;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FlameItem {
    public FlameItem(String name, int value) {
        this.name = name;
        this.value = value;
        this.subMap = new HashMap<>();
    }

    @JsonProperty("n")
    private String name;
    @JsonProperty("v")
    private int value;
    @JsonProperty("c")
    private Collection<FlameItem> sub;

    @JsonIgnore
    private final Map<String, FlameItem> subMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void increase() {
        this.value++;
    }

    public Collection<FlameItem> getSub() {
        return sub;
    }

    public void setSub(Collection<FlameItem> sub) {
        this.sub = sub;
    }

    public Map<String, FlameItem> getSubMap() {
        return subMap;
    }

    public void toStandardFlame() {
        this.sub = this.subMap.values();
        for (FlameItem item : this.sub) {
            item.toStandardFlame();
        }
    }

    public void addSubFlameItem(FlameItem item) {
        this.subMap.put(item.getName(), item);
        this.value += item.getValue();
    }

    public void addFlameItemByRecordedFrame(List<RecordedFrame> frames, String nodeIP, String filename) {
        Map<String, FlameItem> loopMap = subMap;
        loopMap = addFlameOneItem(nodeIP, loopMap);
        loopMap = addFlameOneItem(filename, loopMap);
        for (int i = frames.size() - 1; i >= 0; i--) {
            RecordedFrame frame = frames.get(i);
            String methodName = frame.getMethod().getType().getName() + "." + frame.getMethod().getName();
            String name = JfrMethodSignatureParser
                    .convertMethodSignatureWithoutReturnType(frame.getMethod().getDescriptor(), methodName);
            loopMap = addFlameOneItem(name, loopMap);
        }
        this.increase();
    }

    private static Map<String, FlameItem> addFlameOneItem(String label, Map<String, FlameItem> loopMap) {
        FlameItem item = loopMap.get(label);
        if (item != null) {
            item.increase();
        } else {
            item = new FlameItem(label, 1);
            loopMap.put(label, item);
        }
        loopMap = item.getSubMap();
        return loopMap;
    }
}
