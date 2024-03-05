#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname $0); pwd)

cd ${current_dir}
cp ${current_dir}/tools/download_dependency/src/* ${current_dir}/tools/install_dependency/src/

cd ${current_dir}/tools/download_dependency
pyinstaller -F ./src/download.py -p ./

cd ${current_dir}/tools/install_dependency
pyinstaller -F ./src/devkitpipeline.py -p ./  --add-data "${current_dir}/component:component"

cp ${current_dir}/tools/install_dependency/config/machine.yaml ${current_dir}/tools/install_dependency/dist/machine.yaml
cp -rf ${current_dir}/component ${current_dir}/tools/install_dependency/dist/
cp ${current_dir}/tools/download_dependency/dist/download ${current_dir}/tools/install_dependency/dist/

mkdir -p ${current_dir}/tools/install_dependency/v1.0/tools/
cp -rf  ${current_dir}/tools/install_dependency/dist/  ${current_dir}/tools/install_dependency/v1.0/tools/linux
cd ${current_dir}/tools/install_dependency/v1.0/
tar -zcvf v1.0.tar.gz tools
