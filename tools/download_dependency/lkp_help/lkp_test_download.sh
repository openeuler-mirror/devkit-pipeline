#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

current_dir=$(cd $(dirname $0); pwd)
function main() {
    upload_path=$1
    cd ${current_dir}
    git clone https://gitee.com/wu_fengguang/lkp-tests.git
    cd lkp-tests
    git apply ${current_dir}/devkit-pipeline.patch
    /bin/cp -rf ${current_dir}/compatibility-test ${current_dir}/lkp-tests/programs/compatibility-test
    cd ${current_dir}/
    tar -zcvf lkp-tests.tar.gz ./lkp-tests
    /bin/cp -rf lkp-tests.tar.gz ${upload_path}
}

main "$@"
