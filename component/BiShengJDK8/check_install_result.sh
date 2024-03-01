#!/bin/bash

if [[ -f ${HOME}/.local/bisheng-jdk1.8.0_402/bin/java ]]; then
    echo "true"
else
    echo "false"
fi
