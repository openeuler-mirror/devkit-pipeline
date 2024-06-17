#!/bin/bash

cd /tmp/devkitdependencies/

if [[ ! -d ${HOME}/.local/bisheng-jdk1.8.0_402 ]]; then
    mkdir -p ${HOME}/.local

    echo "Decompress bisheng-jdk-8u402-linux-aarch64.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/bisheng-jdk-8u402-linux-aarch64.tar.gz -C ${HOME}/.local
    echo "Decompress bisheng-jdk-8u402-linux-aarch64.tar.gz to ${HOME}/.local finished."
fi

java_path=$(which java)
if [[ ${java_path} != ${HOME}/.local/bisheng-jdk1.8.0_402/bin/java ]]; then
    sed -i '/bisheng-jdk1.8.0_402/,+3d' ${HOME}/.bashrc

    echo "change ${HOME}/.bashrc"
    cat >> ${HOME}/.bashrc <<'EOF'
export JAVA_HOME=${HOME}/.local/bisheng-jdk1.8.0_402
export PATH=${JAVA_HOME}/bin:${PATH}
export CLASSPATH=.:${JAVA_HOME}/lib/dt.jar:${JAVA_HOME}/lib/tools.jar
export JRE_HOME=${JAVA_HOME}/jre

EOF

    echo "source ${HOME}/.bashrc"
    set +x
    source ${HOME}/.bashrc
fi