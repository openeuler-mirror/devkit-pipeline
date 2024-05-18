#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(realpath "${current_dir}/../..")
umask 077

build_dir=${project_dir}/build/distributor
rm -rf "${build_dir}"
mkdir -p "${build_dir}"

cd "${build_dir}"

pyinstaller -F "${current_dir}"/devkit_tester_agent/bin/flight_records_sample.py --runtime-tmpdir . \
    -p "${project_dir}"/common

mkdir -p devkit_tester_agent/bin
mkdir -p devkit_tester_agent/data
mkdir -p devkit_tester_agent/log

cp "${build_dir}"/dist/flight_records_sample devkit_tester_agent/bin
cp "${current_dir}"/devkit_tester_agent/script/devkit_agent_start.sh devkit_tester_agent/bin
cp -rf "${current_dir}"/devkit_tester_agent/config devkit_tester_agent

tar -czf devkit_tester_agent.tar.gz devkit_tester_agent


pyinstaller -F "${current_dir}"/devkit_tester/bin/entrance.py --runtime-tmpdir . \
    -p "${project_dir}"/common

mkdir -p devkit_tester/bin
mkdir -p devkit_tester/data
mkdir -p devkit_tester/log
mkdir -p devkit_tester/lib

cp "${build_dir}"/dist/entrance devkit_tester/bin
cp -rf "${current_dir}"/devkit_tester/config devkit_tester
cp -rf "${current_dir}"/devkit_tester/script/* devkit_tester/bin
cp devkit_tester_agent.tar.gz devkit_tester/config

bash "${current_dir}"/JFRParser/build.sh

cp -rf "${current_dir}"/JFRParser/target/JFRParser/* devkit_tester

tar -czf devkit_tester.tar.gz devkit_tester

mkdir -p "${project_dir}"/build/component/DevKitTester
cp "${current_dir}/check_install_result.sh" "${project_dir}"/build/component/DevKitTester
cp "${current_dir}/install.sh" "${project_dir}"/build/component/DevKitTester
