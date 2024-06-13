#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -e
current_dir=$(cd $(dirname "$0"); pwd)
project_dir=$(dirname "${current_dir}")
build_dir=${project_dir}/build
final_component_dir=${build_dir}/component

umask 077

function build_devkit_tester() {
    mkdir -p "${final_component_dir}"/DevKitTester
    cp "${current_dir}"/DevKitTester/install.sh "${final_component_dir}"/DevKitTester
    cp "${current_dir}"/DevKitTester/check_install_result.sh "${final_component_dir}"/DevKitTester

    # bash "${current_dir}"/DevKitTester/build_devkit_tester.sh
}

function build_devkit_installer() {
    bash "${current_dir}"/DevKitWeb/build_devkit_installer.sh
}

function handle_compatibility_testing() {
    mkdir -p "${final_component_dir}"/CompatibilityTesting
    cp "${current_dir}"/CompatibilityTesting/install.sh "${final_component_dir}"/CompatibilityTesting
    cp "${current_dir}"/CompatibilityTesting/check_install_result.sh "${final_component_dir}"/CompatibilityTesting
}

function main() {
    if [[ -d ${final_component_dir} ]]; then
        rm -rf "${final_component_dir}"
    fi

    mkdir -p "${final_component_dir}"

    component_arrays=(
        "BiShengCompiler" "BiShengJDK8" "BiShengJDK17" "GCCforOpenEuler"
        "NonInvasiveSwitching" "A-FOT"
        "DevKitCLI" "ClamAV"
    )
    for element in "${component_arrays[@]}"; do
        cp -rf "${current_dir}/${element}" "${final_component_dir}"
    done

    rm -fr "${final_component_dir}/{CompatibilityTesting, DevKitTester, DevKitWeb, OpenEulerMirrorISO}"
    build_devkit_tester
    build_devkit_installer
    handle_compatibility_testing
}

main "$@"
