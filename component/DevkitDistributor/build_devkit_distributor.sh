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

pyinstaller -F "${current_dir}"/devkit_distributor_agent/bin/flight_records_sample.py --runtime-tmpdir . \
    -p "${project_dir}"/common

mkdir -p devkit_distributor_agent/bin
mkdir -p devkit_distributor_agent/data
mkdir -p devkit_distributor_agent/log

cp "${build_dir}"/dist/flight_records_sample devkit_distributor_agent/bin
cp "${current_dir}"/devkit_distributor_agent/script/devkit_agent_start.sh devkit_distributor_agent/bin
cp -rf "${current_dir}"/devkit_distributor_agent/config devkit_distributor_agent

tar -czf devkit_distributor_agent.tar.gz devkit_distributor_agent


pyinstaller -F "${current_dir}"/devkit_distributor/bin/entrance.py --runtime-tmpdir . \
    -p "${project_dir}"/common

mkdir -p devkit_distributor/bin
mkdir -p devkit_distributor/data
mkdir -p devkit_distributor/log

cp "${build_dir}"/dist/entrance devkit_distributor/bin
cp -rf "${current_dir}"/devkit_distributor/config devkit_distributor
cp -rf "${current_dir}"/devkit_distributor/script/* devkit_distributor/bin
cp devkit_distributor_agent.tar.gz devkit_distributor/config

tar -czf devkit_distributor.tar.gz devkit_distributor

mkdir -p "${project_dir}"/build/component/DevkitDistributor
cp "${current_dir}/check_install_result.sh" "${project_dir}"/build/component/DevkitDistributor
cp "${current_dir}/install.sh" "${project_dir}"/build/component/DevkitDistributor
