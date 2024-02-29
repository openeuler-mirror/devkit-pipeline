#!/bin/bash

gcc_path=$(which gcc)
if [[ ${gcc_path} == ${HOME}/compilers/gcc-10.3.1-2023.12-aarch64-linux/bin/gcc ]]; then
    echo "true"
else
    echo "false"
fi
