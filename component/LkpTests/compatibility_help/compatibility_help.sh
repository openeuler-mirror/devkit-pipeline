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
    /bin/cp -rf ${save_path}/compatibility_testing.sh ${save_path}/compatibility_testing/Chinese/compatibility_testing.sh
    echo "download cloudTest"
    wget -c https://gitee.com/jerry-553/lkp_test_devkitpipeline/releases/download/cloud-test/cloudTest.jar
    /bin/cp -rf cloudTest.jar ${save_path}/compatibility_testing/
    /bin/cp -rf ${save_path}/json2html.py ${save_path}/compatibility_testing/
    /bin/cp -rf ${save_path}/template.html.bak ${save_path}/compatibility_testing/
    /bin/cp -rf ${save_path}/report_result.sh ${save_path}/compatibility_testing/
    /bin/cp -rf ${save_path}/check_report_result.py ${save_path}/compatibility_testing/
    /bin/cp -rf ${save_path}/compatibility_testing/template.html.bak ${save_path}/compatibility_testing/template.html
    echo "tar -zcf compatibility_testing.tar.gz ./compatibility_testing"
    tar -zcf compatibility_testing.tar.gz ./compatibility_testing

    echo "/bin/cp -rf compatibility_testing.tar.gz ${upload_path}"
    /bin/cp -rf compatibility_testing.tar.gz ${upload_path}
}

main "$@"
