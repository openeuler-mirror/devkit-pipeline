/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity;

import com.huawei.ic.openlab.cloudtest.util.Constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TestCaseResultCount
 *
 * @author kongcaizhi
 * @since 2021-10-30
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TestCaseResultCount {
    private int total = 0;
    private int passed = 0;
    private int failed = 0;
    private String startTime;

    /**
     * add result
     *
     * @param result result
     */
    public void add(String result) {
        if (Constants.TEST_PASSED.equals(result)) {
            passed += 1;
        } else {
            failed += 1;
        }
        total += 1;
    }
}
