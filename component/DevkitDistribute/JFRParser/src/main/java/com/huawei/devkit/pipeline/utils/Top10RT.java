package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.JmeterRT;
import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import com.huawei.devkit.pipeline.constants.JFRConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class Top10RT {
    public final static int TOP10 = 10;

    public static List<LatencyTopInfo> getTopTen(List<JmeterRT> rtList, int topN) {
        if (rtList == null || rtList.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用最小堆来保持最大的10个元素
        PriorityQueue<JmeterRT> minHeap = new PriorityQueue<>(topN,
                Comparator.comparingDouble(JmeterRT::getResponseTime));

        for (JmeterRT item : rtList) {
            // 如果堆的大小小于10，或者当前数字大于堆顶元素（即当前最小的元素）
            if (minHeap.size() < topN || item.getResponseTime() > Objects.requireNonNull(minHeap.peek()).getResponseTime()) {
                if (minHeap.size() == topN) {
                    // 移除堆顶元素（即当前最小的元素）
                    minHeap.poll();
                }
                // 添加当前数字到堆中
                minHeap.offer(item);
            }

        }

        // 将堆中的元素转换为数组（堆顶是最大的元素）
        List<LatencyTopInfo> topTen = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            JmeterRT jmeterRT = minHeap.poll();
            topTen.add(new LatencyTopInfo(jmeterRT.getStartTime(),
                    jmeterRT.getStartTime() + JFRConstants.MS_1000,
                    jmeterRT.getStartTime()));
        }
        topTen.sort(Comparator.comparingLong(LatencyTopInfo::getStartTime));
        return topTen;
    }
}
