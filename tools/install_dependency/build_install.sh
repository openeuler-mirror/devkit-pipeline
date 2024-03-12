#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(dirname $(dirname "${current_dir}"))

umask 077

build_dir=${project_dir}/build/install_dependency
rm -rf "${build_dir}"
mkdir -p "${build_dir}"

cd "${build_dir}"

pyinstaller -F "${current_dir}"/src/devkitpipeline.py -p "${current_dir}/src:${project_dir}/tools/download_dependency/src" --add-data "../../component:component" --add-data "${project_dir}/tools/download_dependency/lkp_help:lkp_help" --add-data "${project_dir}/tools/download_dependency/compatibility_test_help:compatibility_test_help"

cp "${current_dir}"/config/machine.yaml "${build_dir}"/dist/machine.yaml
