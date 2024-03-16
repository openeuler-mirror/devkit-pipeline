#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
function main() {
    save_path=$1
    upload_path=$2
    cd ${save_path}

    tar -zxf ${save_path}/compatibility_testing.tar.gz
    rm -rf ${save_path}/compatibility_testing/Chinese/compatibility_testing.sh
    cp -rf ${save_path}/compatibility_testing.sh ${save_path}/compatibility_testing/Chinese/compatibility_testing.sh
    cp ${save_path}/json2html.py ${save_path}/compatibility_testing/
    cp ${save_path}/template.html.bak ${save_path}/compatibility_testing/
    /bin/cp -rf ${save_path}/compatibility_testing/template.html.bak ${save_path}/compatibility_testing/template.html
    echo "tar -zcf compatibility_testing.tar.gz ./compatibility_testing"
    tar -zcf compatibility_testing.tar.gz ./compatibility_testing

    echo "/bin/cp -rf compatibility_testing.tar.gz ${upload_path}"
    /bin/cp -rf compatibility_testing.tar.gz ${upload_path}
}

main "$@"
