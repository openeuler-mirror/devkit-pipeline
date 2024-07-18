/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.entity;

import com.huawei.devkit.code.inspector.listener.CustomJsonFormatterLogger;
import com.puppycrawl.tools.checkstyle.AbstractAutomaticBean;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.SarifLogger;
import com.puppycrawl.tools.checkstyle.XMLLogger;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * OutputStyle
 *
 * @since 2024-07-15
 */
public enum OutputStyle {
    /**
     * XML output format.
     */
    XML,
    /**
     * SARIF output format.
     */
    SARIF,
    /**
     * Json output format
     */
    JSON,
    /**
     * Plain output format.
     */
    PLAIN;

    /**
     * Returns a new AuditListener for this OutputFormat.
     *
     * @param out     the output stream
     * @param options the output stream options
     * @return a new AuditListener for this OutputFormat
     * @throws IOException if there is any IO exception during logger initialization
     */
    public AuditListener createListener(
            OutputStream out,
            AbstractAutomaticBean.OutputStreamOptions options) throws IOException {
        final AuditListener result;
        if (this == XML) {
            result = new XMLLogger(out, options);
        } else if (this == SARIF) {
            result = new SarifLogger(out, options);
        } else if (this == JSON) {
            result = new CustomJsonFormatterLogger(out, options);
        } else {
            result = new DefaultLogger(out, options);
        }
        return result;
    }

    /**
     * Returns the name in lowercase.
     *
     * @return the enum name in lowercase
     */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
