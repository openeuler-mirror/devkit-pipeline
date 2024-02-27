#!/bin/bash

#java_path=$(which java)
#if [[ ${java_path} == ${HOME}/compilers/bisheng-jdk1.8.0_402/bin/java ]]; then

if [[ -f ${HOME}/compilers/bisheng-jdk1.8.0_402/bin/java ]]; then
    echo "true"
else
    echo "false"
fi
