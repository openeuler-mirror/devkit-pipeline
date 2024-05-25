package com.huawei.devkit.pipeline.utils;

import com.huawei.devkit.pipeline.bo.JmeterRT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class SimplifyResponseTest {
    @Test
    @DisplayName("simplify:args_0 is null")
    public void testSimplifyThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> SimplifyResponse.simplify(null, JmeterRT.class));
    }

    @Test
    @DisplayName("simplify:args_1 is null")
    public void testSimplifyThrowNullPointerException2() {
        List<JmeterRT> jmeterRTList = new ArrayList<>();
        assertThrows(NullPointerException.class, () -> SimplifyResponse.simplify(jmeterRTList, null));
    }

    @Test
    public void testSimplify() {
        List<JmeterRT> jmeterRTList = new ArrayList<>();
        jmeterRTList.add(new JmeterRT(1L, 1d));
        jmeterRTList.add(new JmeterRT(2L, 2d));
        jmeterRTList.add(new JmeterRT(3L, 3d));
        jmeterRTList.add(new JmeterRT(4L, 4d));
        Map<String, List<Object>> simplified = SimplifyResponse.simplify(jmeterRTList, JmeterRT.class);
        Assertions.assertEquals(simplified.get("responseTime").size(), 4);
        Assertions.assertEquals(simplified.get("responseTime").toString(), List.of(1d, 2d, 3d, 4d).toString());
    }
}
