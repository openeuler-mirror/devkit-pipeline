#!/bin/bash

clang_path=$(which clang)
if [[ ${clang_path} == ${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang ]]; then
    echo "true"
else
    echo "false"
fi
