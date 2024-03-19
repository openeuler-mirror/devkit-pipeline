#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

current_dir=$(cd $(dirname $0); pwd)
function main() {
    upload_path=$1
    cd ${current_dir}
    wget -c https://gitee.com/jerry-553/lkp_test_devkitpipeline/releases/download/lkp-all-resource/lkp-tests.tar.gz
    /bin/cp -rf lkp-tests.tar.gz ${upload_path}
}

main "$@"
