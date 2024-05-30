#!/bin/bash

devkit_path=$(which devkit)
if [[ -f ${HOME}/.local/DevKit-CLI-24.0.RC1-Linux-Kunpeng/devkit ]] && [[ ${devkit_path} == /usr/local/bin/devkit ]]; then
    echo "true"
else
    echo "false"
fi