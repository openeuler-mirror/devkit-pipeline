#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
function main() {
  save_path=$1
  upload_path=$2
  cd ${save_path}
  tar -zvxf ${save_path}/compatibility_testing.tar.gz
  rm -rf ${save_path}/compatibility_testing/Chinese/compatibility_testing.sh
  mv ${save_path}/compatibility_testing.sh ${save_path}/compatibility_testing/Chinese/compatibility_testing.sh
  mv ${save_path}/json2html.py ${save_path}/compatibility_testing/
  mv ${save_path}/check_report_result.py ${save_path}/compatibility_testing/
  mv ${save_path}/report_result.sh ${save_path}/compatibility_testing/
  mv ${save_path}/template.html.bak ${save_path}/compatibility_testing/
  /bin/cp ${save_path}/compatibility_testing/template.html.bak ${save_path}/compatibility_testing/template.html
  tar -zcvf compatibility_testing.tar.gz ./compatibility_testing
  /bin/cp -rf compatibility_testing.tar.gz ${upload_path}
}

main "$@"

