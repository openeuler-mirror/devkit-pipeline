#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(realpath "${current_dir}/../..")
umask 077

build_dir=${project_dir}/build/distribute
rm -rf "${build_dir}"
mkdir -p "${build_dir}"

cd "${build_dir}"

pyinstaller -F "${current_dir}"/devkit_pipeline_agent/bin/flight_records_sample.py --runtime-tmpdir . \
    -p "${project_dir}"/common

mkdir -p devkit_pipeline_agent/bin
mkdir -p devkit_pipeline_agent/data
mkdir -p devkit_pipeline_agent/log

cp "${build_dir}"/dist/flight_records_sample devkit_pipeline_agent/bin
cp "${current_dir}"/devkit_pipeline_agent/script/devkit_agent_start.sh devkit_pipeline_agent/bin
cp -rf "${current_dir}"/devkit_pipeline_agent/config devkit_pipeline_agent

tar -czf devkit_pipeline_agent.tar.gz devkit_pipeline_agent


pyinstaller -F "${current_dir}"/devkit_distribute/bin/entrance.py --runtime-tmpdir . \
    -p "${project_dir}"/common

mkdir -p devkit_distribute/bin
mkdir -p devkit_distribute/data
mkdir -p devkit_distribute/log

cp "${build_dir}"/dist/entrance devkit_distribute/bin
cp -rf "${current_dir}"/devkit_distribute/config devkit_distribute
cp -rf "${current_dir}"/devkit_distribute/script/* devkit_distribute/bin
cp devkit_pipeline_agent.tar.gz devkit_distribute/config

tar -czf devkit_distribute.tar.gz devkit_distribute

mkdir -p "${project_dir}"/build/component/DevkitDistribute
cp "${current_dir}/check_install_result.sh" "${project_dir}"/build/component/DevkitDistribute
cp "${current_dir}/install.sh" "${project_dir}"/build/component/DevkitDistribute
