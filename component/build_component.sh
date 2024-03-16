#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(realpath "${current_dir}/..")
umask 077

mkdir -p "${project_dir}"/build/component

component_arrays=(
    "BiShengCompiler" "BiShengJDK8" "BiShengJDK17" "CompatibilityTesting" "GCCforOpenEuler" "DevkitDistribute"
    "LkpTests" "OpenEulerMirrorISO"
)

for element in "${component_arrays[@]}";
  do
  cp -rf "${project_dir}/component/${element}" "${project_dir}"/build/component
  done


