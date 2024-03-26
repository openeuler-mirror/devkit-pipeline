#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)

tag="v0.2"

mkdir -p "${current_dir}"/build

bash "${current_dir}"/component/build_component.sh

bash "${current_dir}"/tools/download_dependency/build_download.sh

bash "${current_dir}"/tools/install_dependency/build_install.sh

cd "${current_dir}"/build

mkdir -p "${current_dir}"/build/devkit-pipeline-${tag}/linux
cp -rf  "${current_dir}"/build/install_dependency/dist/*  "${current_dir}"/build/devkit-pipeline-${tag}/linux
cp -rf  "${current_dir}"/build/download_dependency/dist/*  "${current_dir}"/build/devkit-pipeline-${tag}/linux

tar -zcvf devkit-pipeline-${tag}.tar.gz devkit-pipeline-${tag}
