#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

cd ${HOME}/.local/compatibility_testing/
result=`python3 ${HOME}/.local/compatibility_testing/check_report_result.py`

if [ "$result" == "-1" ]; then
  exit 1
fi