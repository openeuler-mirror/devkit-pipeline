#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(dirname $(dirname "${current_dir}"))

umask 077

build_dir=${project_dir}/build/devkit_installer
rm -rf "${build_dir}"
mkdir -p "${build_dir}"

cd "${build_dir}"

pyinstaller -F "${current_dir}"/devkit_installer/devkit_installer.py -p "${current_dir}"/devkit_installer  --name "devkit_installer"

mkdir -p "${project_dir}"/build/component/DevKitWeb
cp "${build_dir}"/dist/devkit_installer "${project_dir}"/build/component/DevKitWeb
