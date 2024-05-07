package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.JmeterRT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Top10RT {

    public static List<JmeterRT> getTopTen(List<JmeterRT> rtList) {
        if (rtList == null || rtList.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用最小堆来保持最大的10个元素
        PriorityQueue<JmeterRT> minHeap = new PriorityQueue<>(10,
                Comparator.comparingDouble(JmeterRT::getResponseTime));

        for (JmeterRT item : rtList) {
            // 如果堆的大小小于10，或者当前数字大于堆顶元素（即当前最小的元素）
            if (minHeap.size() < 10 || item.getResponseTime() > minHeap.peek().getResponseTime()) {
                if (minHeap.size() == 10) {
                    // 移除堆顶元素（即当前最小的元素）
                    minHeap.poll();
                }
                // 添加当前数字到堆中
                minHeap.offer(item);
            }
        }

        // 将堆中的元素转换为数组（堆顶是最大的元素）
        List<JmeterRT> topTen = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            topTen.add(minHeap.poll());
        }
        topTen.sort(Comparator.comparingLong(JmeterRT::getStartTime));
        return topTen;
    }
}
