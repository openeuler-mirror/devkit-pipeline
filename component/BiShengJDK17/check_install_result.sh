#!/bin/bash

if [[ -f ${HOME}/.local/bisheng-jdk-17.0.10/bin/java ]]; then
    echo "true"
else
    echo "false"
fi
