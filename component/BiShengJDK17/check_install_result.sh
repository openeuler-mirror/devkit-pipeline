#!/bin/bash

#java_path=$(which java)
#if [[ ${java_path} == ${HOME}/compilers/bisheng-jdk-17.0.10/bin/java ]]; then

if [[ -f ${HOME}/compilers/bisheng-jdk-17.0.10/bin/java ]]; then
    echo "true"
else
    echo "false"
fi
