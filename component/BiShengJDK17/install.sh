#!/bin/bash

cd /tmp/devkitdependencies/

if [[ ! -d ${HOME}/.local/bisheng-jdk-17.0.10 ]]; then
    mkdir -p ${HOME}/.local

    echo "Decompress bisheng-jdk-17.0.10-linux-aarch64.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/bisheng-jdk-17.0.10-linux-aarch64.tar.gz -C ${HOME}/.local
    echo "Decompress bisheng-jdk-17.0.10-linux-aarch64.tar.gz to ${HOME}/.local finished."
fi

java_path=$(which java)
if [[ ${java_path} != ${HOME}/.local/bisheng-jdk-17.0.10/bin/java ]]; then
    sed -i '/bisheng-jdk-17.0.10/d' ${HOME}/.bashrc

    echo "change ${HOME}/.bashrc"
    cat >> ${HOME}/.bashrc <<'EOF'
export JAVA_HOME=${HOME}/.local/bisheng-jdk-17.0.10
export PATH=${JAVA_HOME}/bin:${PATH}
EOF

    echo "source ${HOME}/.bashrc"
    set +x
    source ${HOME}/.bashrc
fi
