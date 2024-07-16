/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector;

import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class CodeInspectorTestCase {
    @Nullable private String aab;

    @Test void test01() {
        System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();System.out.println();
    }


    @Test
    void test02(@Nullable String demo) {
        System.out.println(demo);
    }

    void test03(@Nullable String demo) {
        System.out.println(demo);
    }
}
