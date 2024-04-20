package com.huawei.devkit.pipeline.bo;

import jdk.jfr.consumer.RecordedFrame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FlameItem {
    public FlameItem(String name, int value) {
        this.name = name;
        this.value = value;
        this.subMap = new HashMap<>();
    }

    private String name;
    private int value;
    private List<FlameItem> sub;

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

    public List<FlameItem> getSub() {
        return sub;
    }

    public void setSub(List<FlameItem> sub) {
        this.sub = sub;
    }

    public Map<String, FlameItem> getSubMap() {
        return subMap;
    }

    public void addFlameItem(List<RecordedFrame> frames) {
        Map<String, FlameItem> loopMap = subMap;
        for (int i = frames.size() - 1; i >= 0; i--) {
            RecordedFrame frame = frames.get(i);
            String name = frame.getMethod().getType().getName() + "." + frame.getMethod().getName() + ":" + frame.getLineNumber();
            FlameItem item = loopMap.get(name);
            if (item != null) {
                item.increase();
            } else {
                item = new FlameItem(name, 1);
                loopMap.put(name, item);
            }
            loopMap = item.getSubMap();
        }
        this.increase();
    }
}
