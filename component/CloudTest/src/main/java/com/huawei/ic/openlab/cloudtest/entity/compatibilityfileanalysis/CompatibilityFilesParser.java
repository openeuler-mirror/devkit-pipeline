/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.entity.compatibilityfileanalysis;

import java.io.IOException;

/**
 * CompatibilityFilesParser
 *
 * @author kongcaizhi
 * @since 2021-12-15
 */
public interface CompatibilityFilesParser {
    /**
     * Get result
     *
     * @return CompatibilityTestResult
     * @throws IOException exception
     */
    CompatibilityTestResult getResult() throws IOException;

    /**
     * parseFiles
     *
     * @return CompatibilityTestResult
     * @throws IOException exception
     */
    CompatibilityTestResult parseFiles() throws IOException;
}
