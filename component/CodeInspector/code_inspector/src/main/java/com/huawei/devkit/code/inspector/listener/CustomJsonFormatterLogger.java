/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.listener;

import com.huawei.devkit.code.inspector.utils.PropertiesUtils;
import com.puppycrawl.tools.checkstyle.AbstractAutomaticBean.OutputStreamOptions;
import com.puppycrawl.tools.checkstyle.SarifLogger;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DataBaseListener
 *
 * @since 2024-07-11
 */
@Slf4j
public class CustomJsonFormatterLogger implements AuditListener {
    /**
     * The placeholder for message.
     */
    private static final String MESSAGE_PLACEHOLDER = "${message}";

    /**
     * The placeholder for severity level.
     */
    private static final String SEVERITY_LEVEL_PLACEHOLDER = "${severityLevel}";

    /**
     * The placeholder for uri.
     */
    private static final String URI_PLACEHOLDER = "${uri}";

    /**
     * The placeholder for line.
     */
    private static final String LINE_PLACEHOLDER = "${line}";

    /**
     * The placeholder for column.
     */
    private static final String COLUMN_PLACEHOLDER = "${column}";

    /**
     * The placeholder for rule id.
     */
    private static final String RULE_ID_PLACEHOLDER = "${ruleId}";

    /**
     * The placeholder for results.
     */
    private static final String RESULTS_PLACEHOLDER = "${results}";
    /**
     * Helper writer that allows easy encoding and printing.
     */
    private final PrintWriter writer;

    /**
     * Close output stream in auditFinished.
     */
    private final boolean closeStream;
    /**
     * Content for the entire report.
     */
    private final String report;

    /**
     * Content for result representing an error with source line and column.
     */
    private final String resultLineColumn;

    /**
     * Content for result representing an error with source line only.
     */
    private final String resultLineOnly;

    /**
     * Content for result representing an error with filename only and without source location.
     */
    private final String resultFileOnly;

    /**
     * Content for result representing an error without filename or location.
     */
    private final String resultErrorOnly;

    private final List<String> results = new ArrayList<>();

    public CustomJsonFormatterLogger(OutputStream outputStream,
                                     OutputStreamOptions outputStreamOptions) throws IOException {
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        closeStream = outputStreamOptions == OutputStreamOptions.CLOSE;
        report = PropertiesUtils.readResource("templates/CustomReport.template");
        resultLineColumn =
                PropertiesUtils.readResource("templates/ResultLineColumn.template");
        resultLineOnly =
                PropertiesUtils.readResource("templates/ResultLineOnly.template");
        resultFileOnly =
                PropertiesUtils.readResource("templates/ResultFileOnly.template");
        resultErrorOnly =
                PropertiesUtils.readResource("templates/ResultErrorOnly.template");

    }


    @Override
    public void auditStarted(AuditEvent event) {

    }

    @Override
    public void auditFinished(AuditEvent event) {
        final String rendered = report.replace(RESULTS_PLACEHOLDER, String.join(",\n", results));
        writer.print(rendered);
        if (closeStream) {
            writer.close();
        } else {
            writer.flush();
        }
    }

    @Override
    public void fileStarted(AuditEvent event) {

    }

    @Override
    public void fileFinished(AuditEvent event) {

    }

    @Override
    public void addError(AuditEvent event) {
        if (event.getColumn() > 0) {
            results.add(resultLineColumn
                    .replace(SEVERITY_LEVEL_PLACEHOLDER, event.getSeverityLevel().getName())
                    .replace(URI_PLACEHOLDER, event.getFileName())
                    .replace(COLUMN_PLACEHOLDER, Integer.toString(event.getColumn()))
                    .replace(LINE_PLACEHOLDER, Integer.toString(event.getLine()))
                    .replace(MESSAGE_PLACEHOLDER, SarifLogger.escape(event.getMessage()))
                    .replace(RULE_ID_PLACEHOLDER,
                            event.getModuleId() != null ? event.getModuleId() : event.getViolation().getKey())
            );
        } else {
            results.add(resultLineOnly
                    .replace(SEVERITY_LEVEL_PLACEHOLDER, event.getSeverityLevel().getName())
                    .replace(URI_PLACEHOLDER, event.getFileName())
                    .replace(LINE_PLACEHOLDER, Integer.toString(event.getLine()))
                    .replace(MESSAGE_PLACEHOLDER, SarifLogger.escape(event.getMessage()))
                    .replace(RULE_ID_PLACEHOLDER,
                            event.getModuleId() != null ? event.getModuleId() : event.getViolation().getKey())
            );
        }
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printer = new PrintWriter(stringWriter);
        throwable.printStackTrace(printer);
        if (event.getFileName() == null) {
            results.add(resultErrorOnly
                    .replace(SEVERITY_LEVEL_PLACEHOLDER, event.getSeverityLevel().getName())
                    .replace(MESSAGE_PLACEHOLDER, SarifLogger.escape(stringWriter.toString()))
            );
        } else {
            results.add(resultFileOnly
                    .replace(SEVERITY_LEVEL_PLACEHOLDER, event.getSeverityLevel().getName())
                    .replace(URI_PLACEHOLDER, event.getFileName())
                    .replace(MESSAGE_PLACEHOLDER, SarifLogger.escape(stringWriter.toString()))
            );
        }
    }
}
