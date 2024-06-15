#!/bin/bash

java_path=$(which java)
if [[ ${java_path} == ${HOME}/.local/bisheng-jdk-17.0.10/bin/java ]]; then
    echo "true"
else
    if [[ -f ${HOME}/.local/bisheng-jdk-17.0.10/bin/java ]] && [[ $(grep -A1 '^export JAVA_HOME=${HOME}/.local/bisheng-jdk-17.0.10$' ${HOME}/.bashrc | grep '^export PATH=${JAVA_HOME}/bin:${PATH}$' | wc -l) == "1" ]]; then
        echo "true"
    else
        echo "false"
    fi
fi
