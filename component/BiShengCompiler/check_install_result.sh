#!/bin/bash

clang_path=$(which clang)
if [[ ${clang_path} == ${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang ]]; then
    echo "true"
else
    if [[ -f ${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang ]] && [[ $(grep -A1 '^export BISHENG_COMPILER_HOME=${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux/bin$' ${HOME}/.bashrc | grep '^export PATH=${BISHENG_COMPILER_HOME}:${PATH}$' | wc -l) == "1" ]]; then
        echo "true"
    else
        echo "false"
    fi
fi
