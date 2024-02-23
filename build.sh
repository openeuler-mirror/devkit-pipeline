#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname $0); pwd)

cd $current_dir
cp $current_dir/tools/download_dependency/src/* $current_dir/tools/install_dependency/src/

cd $current_dir/tools/download_dependency
pyinstaller -F ./src/download.py -p ./

cd $current_dir/tools/install_dependency
pyinstaller -F ./src/devkitpipeline.py -p ./
