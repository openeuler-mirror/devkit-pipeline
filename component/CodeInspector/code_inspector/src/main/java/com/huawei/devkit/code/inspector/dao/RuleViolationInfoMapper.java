/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024. All rights reserved.
 */

package com.huawei.devkit.code.inspector.dao;

import com.huawei.devkit.code.inspector.entity.RuleViolationInfo;

import java.util.List;

/**
 * RuleViolationInfoMapper
 *
 * @since 2024-07-11
 */
public interface RuleViolationInfoMapper {

    int addRuleViolationInfos(List<RuleViolationInfo> infoList);
}
