#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

current_dir=$(cd $(dirname $0); pwd)

function main() {
    upload_path=$1
    echo "build LkpTests upload_path: " "${upload_path}"
    sh ${current_dir}/lkp_help/lkp_help.sh ${upload_path}

    cd ${current_dir}/compatibility_help/
    wget -c https://mirrors.huaweicloud.com/kunpeng/archive/compatibility_testing/compatibility_testing.tar.gz
    sh ${current_dir}/compatibility_help/compatibility_help.sh  ${current_dir}/compatibility_help  ${upload_path}

    /bin/cp -rf ${current_dir}/gem_dependencies.zip ${upload_path}
}

main "$@"
