#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

current_dir=$(cd $(dirname $0); pwd)

function main() {
    upload_path=$1
    cd ${current_dir}
    if [[ -d ${current_dir}/lkp-tests ]]; then
        rm -rf ${current_dir}/lkp-tests
    fi

    git clone https://gitee.com/wu_fengguang/lkp-tests.git
    if [[ "$?" -ne "0" ]]; then
        exit 1
    fi

    cd lkp-tests
    git apply ${current_dir}/devkit-pipeline.patch
    /bin/cp -rf ${current_dir}/compatibility-test ${current_dir}/lkp-tests/programs/compatibility-test
    dos2unix ${current_dir}/lkp-tests/programs/compatibility-test/run
    if [[ "$?" -ne "0" ]]; then
        exit 1
    fi

    cd ${current_dir}
    echo "tar -zcf lkp-tests.tar.gz ./lkp-tests"
    tar -zcf lkp-tests.tar.gz ./lkp-tests
    if [[ "$?" -ne "0" ]]; then
        exit 1
    fi

    echo "/bin/cp -rf lkp-tests.tar.gz ${upload_path}"
    /bin/cp -rf lkp-tests.tar.gz ${upload_path}
}

main "$@"
