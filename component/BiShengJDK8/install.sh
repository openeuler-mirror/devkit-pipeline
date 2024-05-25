#!/bin/bash

cd /tmp/devkitdependencies/

if [[ ! -d ${HOME}/.local/bisheng-jdk1.8.0_402 ]]; then
    mkdir -p ${HOME}/.local

    echo "Decompress bisheng-jdk-8u402-linux-aarch64.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/bisheng-jdk-8u402-linux-aarch64.tar.gz -C ${HOME}/.local
    echo "Decompress bisheng-jdk-8u402-linux-aarch64.tar.gz to ${HOME}/.local finished."
fi
