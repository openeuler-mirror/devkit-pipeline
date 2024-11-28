#!/bin/bash

devkit_path=$(which devkit)
if [[ ${devkit_path} == ${HOME}/.local/DevKit-CLI-24.0.RC3-Linux-Kunpeng/devkit ]]; then
    echo "true"
else
    echo "false"
fi