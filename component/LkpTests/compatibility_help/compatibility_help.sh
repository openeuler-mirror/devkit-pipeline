#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
function main() {
    save_path=$1
    upload_path=$2
    cd ${save_path}

    echo "/bin/cp -rf compatibility_testing.tar.gz ${upload_path}"
    /bin/cp -rf compatibility_testing.tar.gz ${upload_path}
}

main "$@"
