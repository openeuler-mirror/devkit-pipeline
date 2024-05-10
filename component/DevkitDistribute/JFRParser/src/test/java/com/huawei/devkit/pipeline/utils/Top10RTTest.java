package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.JmeterRT;
import com.huawei.devkit.pipeline.bo.LatencyTopInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.utbot.runtime.utils.java.UtUtils.deepEquals;

public final class Top10RTTest {

    /**
     * @utbot.classUnderTest {@link Top10RT}
     * @utbot.methodUnderTest {@link Top10RT#getTopTen(java.util.List, int)}
     * @utbot.executesCondition {@code (rtList == null): False}
     * @utbot.executesCondition {@code (rtList.isEmpty()): True}
     * @utbot.invokes {@link java.util.List#isEmpty()}
     * @utbot.returnsFrom {@code return new ArrayList<>();}
     */
    @Test
    @DisplayName("getTopTen: rtList == null : True -> return new ArrayList<>()")
    public void testGetTopTenRtListIsEmpty() {
        List<JmeterRT> rtList = new ArrayList<>();
        List<LatencyTopInfo> actual = Top10RT.getTopTen(rtList, 10);
        assertTrue(actual.isEmpty());
    }

    /**
     * @utbot.classUnderTest {@link Top10RT}
     * @utbot.methodUnderTest {@link Top10RT#getTopTen(java.util.List, int)}
     * @utbot.executesCondition {@code (rtList == null): True}
     * @utbot.returnsFrom {@code return new ArrayList<>();}
     */
    @Test
    @DisplayName("getTopTen: rtList == null : False -> return new ArrayList<>()")
    public void testGetTopTenRtListEqualsNull() {
        List<LatencyTopInfo> actual = Top10RT.getTopTen(null, 10);
        assertTrue(actual.isEmpty());
    }

    /**
     * 获取一个top元素
     *
     * @utbot.classUnderTest {@link Top10RT}
     * @utbot.methodUnderTest {@link Top10RT#getTopTen(java.util.List, int)}
     */
    @Test
    @DisplayName("getTopTen: arg_0 = collection only one element in this，arg1 = 10")
    public void testGetTopTen() {
        List<JmeterRT> rtList = new ArrayList<>();
        JmeterRT jmeterRT = new JmeterRT(Long.MAX_VALUE, Double.NaN);
        rtList.add(jmeterRT);
        List<LatencyTopInfo> actual = Top10RT.getTopTen(rtList, 10);
        List<LatencyTopInfo> expected = new ArrayList<>();
        LatencyTopInfo latencyTopInfo = new LatencyTopInfo(Long.MAX_VALUE, -9223372036854774809L, Long.MAX_VALUE);
        expected.add(latencyTopInfo);
        assertTrue(deepEquals(expected, actual));
    }

    /**
     * 获取5个top元素
     *
     * @utbot.classUnderTest {@link Top10RT}
     * @utbot.methodUnderTest {@link Top10RT#getTopTen(java.util.List, int)}
     */
    @Test
    @DisplayName("getTopTen: arg_0 = 5 element collection，arg1 = 5")
    public void testGetTopTen1() {
        List<JmeterRT> rtList = new ArrayList<>();
        JmeterRT jmeterRT = new JmeterRT(1L, -1.0);
        rtList.add(jmeterRT);
        JmeterRT jmeterRT1 = new JmeterRT(9L, 0.0);
        rtList.add(jmeterRT1);
        JmeterRT jmeterRT2 = new JmeterRT(10L, 1.0);
        rtList.add(jmeterRT2);
        JmeterRT jmeterRT3 = new JmeterRT(9L, Double.NEGATIVE_INFINITY);
        rtList.add(jmeterRT3);
        JmeterRT jmeterRT4 = new JmeterRT(Long.MAX_VALUE, Double.NaN);
        rtList.add(jmeterRT4);

        List<LatencyTopInfo> actual = Top10RT.getTopTen(rtList, 5);

        List<LatencyTopInfo> expected = getLatencyTopInfos();
        assertTrue(deepEquals(expected, actual));
    }


    /**
     * 获取 2个top元素
     */
    @Test
    @DisplayName("getTopTen: arg_0 = 5 element collection，arg1 = 2")
    public void testGetTopTen2() {
        List<JmeterRT> rtList = new ArrayList<>();
        JmeterRT jmeterRT = new JmeterRT(1L, -1.0);
        rtList.add(jmeterRT);
        JmeterRT jmeterRT1 = new JmeterRT(9L, 0.0);
        rtList.add(jmeterRT1);
        JmeterRT jmeterRT2 = new JmeterRT(10L, 1.0);
        rtList.add(jmeterRT2);
        JmeterRT jmeterRT3 = new JmeterRT(5L, 2);
        rtList.add(jmeterRT3);
        JmeterRT jmeterRT4 = new JmeterRT(2, 10);
        rtList.add(jmeterRT4);

        List<LatencyTopInfo> actual = Top10RT.getTopTen(rtList, 2);
        List<LatencyTopInfo> expected = new ArrayList<>();
        expected.add(new LatencyTopInfo(2L, 1002L, 2L));
        expected.add(new LatencyTopInfo(5L, 1005L, 5L));
        assertTrue(deepEquals(expected, actual));
    }

    private static List<LatencyTopInfo> getLatencyTopInfos() {
        List<LatencyTopInfo> expected = new ArrayList<>();
        LatencyTopInfo latencyTopInfo = new LatencyTopInfo(1L, 1001L, 1L);
        expected.add(latencyTopInfo);
        LatencyTopInfo latencyTopInfo1 = new LatencyTopInfo(9L, 1009L, 9L);
        expected.add(latencyTopInfo1);
        LatencyTopInfo latencyTopInfo2 = new LatencyTopInfo(9L, 1009L, 9L);
        expected.add(latencyTopInfo2);
        LatencyTopInfo latencyTopInfo3 = new LatencyTopInfo(10L, 1010L, 10L);
        expected.add(latencyTopInfo3);
        LatencyTopInfo latencyTopInfo4 = new LatencyTopInfo(Long.MAX_VALUE, -9223372036854774809L, Long.MAX_VALUE);
        expected.add(latencyTopInfo4);
        return expected;
    }

}
