#!/bin/bash

cd /tmp/devkitdependencies/

if [[ ! -d ${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux ]]; then
    mkdir -p ${HOME}/.local

    echo "Decompress gcc-10.3.1-2023.12-aarch64-linux.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/gcc-10.3.1-2023.12-aarch64-linux.tar.gz -C ${HOME}/.local
    echo "Decompress gcc-10.3.1-2023.12-aarch64-linux.tar.gz to ${HOME}/.local finished."
fi

gcc_path=$(which gcc)
if [[ ${gcc_path} != ${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin/gcc ]]; then
    sed -i '/.*export GCC_HOME=${HOME}\/.local/d' ${HOME}/.bashrc
    sed -i '/.*export .*=${GCC_HOME}.*/d' ${HOME}/.bashrc

    echo "change ${HOME}/.bashrc"
    cat >> ${HOME}/.bashrc <<'EOF'
export GCC_HOME=${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux
export PATH=${GCC_HOME}/bin:${PATH}
export INCLUDE=${GCC_HOME}/include:${INCLUDE}
export LD_LIBRARY_PATH=${GCC_HOME}/lib64:${LD_LIBRARY_PATH}
EOF
    echo "source ${HOME}/.bashrc"
    set +x
    source ${HOME}/.bashrc
else
    echo "install gcc-10.3.1-2023.12 success."
fi
