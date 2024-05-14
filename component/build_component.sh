#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(dirname "${current_dir}")
build_dir=${project_dir}/build
final_component_dir=${build_dir}/component

umask 077

function build_lkp_tests() {
    # 单独处理LkpTests
    mkdir -p "${final_component_dir}"/LkpTests

    cp -rf "${current_dir}"/LkpTests/install.sh "${final_component_dir}"/LkpTests
    cp -rf "${current_dir}"/LkpTests/check_install_result.sh "${final_component_dir}"/LkpTests
}

function build_devkit_distributor() {
    bash "${current_dir}"/DevKitDistributor/build_devkit_distributor.sh
}

function build_devkit_installer() {
    bash "${current_dir}"/DevKitWeb/build_devkit_installer.sh
}

function main() {
    if [[ -d ${final_component_dir} ]]; then
        rm -rf "${final_component_dir}"
    fi

    mkdir -p "${final_component_dir}"

    component_arrays=(
        "BiShengCompiler" "BiShengJDK8" "BiShengJDK17" "GCCforOpenEuler" "OpenEulerMirrorISO" "CompatibilityTesting" "NonInvasiveSwitching" "A-FOT"
        "DevKitCLI" "clamAV"
    )
    for element in "${component_arrays[@]}"; do
        cp -rf "${current_dir}/${element}" "${final_component_dir}"
    done

    build_lkp_tests
    build_devkit_distributor
    build_devkit_installer
}

main "$@"
