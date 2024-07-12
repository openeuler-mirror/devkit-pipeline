/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.listener;

import com.google.common.io.Files;
import com.huawei.devkit.code.inspector.entity.RuleViolationInfo;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DataBaseListener
 *
 * @since 2024-07-11
 */
@Log4j2
public class DataBaseListener implements AuditListener {
    private final String mergeId;
    private List<RuleViolationInfo> infoList;

    public DataBaseListener(String mergeId) {
        this.infoList = new ArrayList<>();
        this.mergeId = mergeId;
    }

    @Override
    public void auditStarted(AuditEvent event) {

    }

    @Override
    public void auditFinished(AuditEvent event) {
        long time = System.currentTimeMillis();

    }

    @Override
    public void fileStarted(AuditEvent event) {

    }

    @Override
    public void fileFinished(AuditEvent event) {

    }

    @Override
    public void addError(AuditEvent event) {
        RuleViolationInfo ruleViolation = RuleViolationInfo.builder().filePath(event.getFileName())
                .lineno(event.getLine())
                .message(event.getMessage()).ruleId(event.getViolation().getModuleId())
                .mergeId(this.mergeId).shielded(false).commitRequestToShield(false).build();
        this.infoList.add(ruleViolation);
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {

    }

    private void filledLine(String file, List<RuleViolationInfo> infoList) {
        try {
            List<String> lines = Files.readLines(new File(file), StandardCharsets.UTF_8);
            for (RuleViolationInfo info : infoList) {
                info.setLine(lines.get(info.getLineno()));
            }
        } catch (IOException ex) {
            log.error("filled line", ex);
        }
    }
}
