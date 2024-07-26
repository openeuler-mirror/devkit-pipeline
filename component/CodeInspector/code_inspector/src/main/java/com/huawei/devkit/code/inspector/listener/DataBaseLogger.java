/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.listener;

import com.google.common.io.Files;
import com.huawei.devkit.code.inspector.dao.RuleViolationInfoMapper;
import com.huawei.devkit.code.inspector.entity.RuleViolationInfo;
import com.huawei.devkit.code.inspector.perload.DataBasePreLoad;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DataBaseListener
 *
 * @since 2024-07-11
 */
@Slf4j
public class DataBaseLogger implements AuditListener {

    private final long time;
    private final String mergeId;
    private final List<RuleViolationInfo> infoList;

    public DataBaseLogger(String mergeId) {
        this.infoList = new ArrayList<>();
        this.mergeId = mergeId;
        this.time = System.currentTimeMillis();
    }

    @Override
    public void auditStarted(AuditEvent event) {

    }

    @Override
    public void auditFinished(AuditEvent event) {
        Map<String, List<RuleViolationInfo>> collected = this.infoList.stream()
            .collect(Collectors.groupingBy(RuleViolationInfo::getFilePath));
        for (Map.Entry<String, List<RuleViolationInfo>> entry : collected.entrySet()) {
            this.filledLine(entry.getKey(), entry.getValue());
        }
        try (SqlSession sqlSession = DataBasePreLoad.getSqlSession()) {
            RuleViolationInfoMapper mapper = sqlSession.getMapper(RuleViolationInfoMapper.class);
            for (int i = 0; i < this.infoList.size(); i += 100) {
                int endIndex = Math.min(i + 100, this.infoList.size());
                mapper.addRuleViolationInfos(this.infoList.subList(i, endIndex));
            }
            sqlSession.commit();
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
        if (SeverityLevel.IGNORE == event.getSeverityLevel()) {
            return;
        }
        RuleViolationInfo ruleViolation = RuleViolationInfo.builder().filePath(event.getFileName())
            .lineno(event.getLine()).time(this.time).filePathHash(event.getFileName().hashCode())
            .message(event.getMessage()).ruleId(event.getViolation().getModuleId())
            .mergeId(this.mergeId).shielded(false).commitRequestToShield(false)
            .level(SeverityLevel.ERROR == event.getSeverityLevel() ? 1 : 0).build();
        this.infoList.add(ruleViolation);
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        log.error("entry", throwable);
    }

    private void filledLine(String file, List<RuleViolationInfo> infoList) {
        try {
            List<String> lines = Files.readLines(new File(file), StandardCharsets.UTF_8);
            for (RuleViolationInfo info : infoList) {
                info.setLine(lines.get(info.getLineno() - 1));
            }
        } catch (IOException ex) {
            log.error("filled line", ex);
        }
    }
}
