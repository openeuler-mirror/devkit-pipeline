#!/bin/bash

cd /tmp/devkitdependencies/

if [[ ! -d ${HOME}/.local/bisheng-jdk-17.0.10 ]]; then
    mkdir -p ${HOME}/.local

    echo "Decompress bisheng-jdk-17.0.10-linux-aarch64.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/bisheng-jdk-17.0.10-linux-aarch64.tar.gz -C ${HOME}/.local
    echo "Decompress bisheng-jdk-17.0.10-linux-aarch64.tar.gz to ${HOME}/.local finished."
fi
