#!/bin/bash

gcc_path=$(which gcc)
if [[ ${gcc_path} == ${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin/gcc ]]; then
    echo "true"
else
    if [[ -f ${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin/gcc ]] && [[ $(grep -A1 '^export GCC_HOME=${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin$' ${HOME}/.bashrc | grep '^export PATH=${GCC_HOME}:${PATH}$' | wc -l) == "1" ]]; then
        echo "true"
    else
        echo "false"
    fi
fi
