#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(dirname $(dirname "${current_dir}"))

umask 077

build_dir=${project_dir}/build/deploy_tool
rm -rf "${build_dir}"
mkdir -p "${build_dir}"

cd "${build_dir}"

pyinstaller -F "${current_dir}"/src/deploy_main.py -p "${current_dir}/src" --add-data "../../build/component:component"  --name "deploy_tool"

cp "${current_dir}"/config/machine.yaml "${build_dir}"/dist/machine.yaml
