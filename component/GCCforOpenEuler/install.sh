#!/bin/bash

cd /tmp/devkitdependencies/
verify_signature=$(sha256sum -c gcc-10.3.1-2023.12-aarch64-linux.tar.gz.sha256 >/dev/null 2>&1; echo $?)
if [[ ${verify_signature} -eq "0" ]]; then
    if [[ ! -d ${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux ]]; then
        mkdir -p ${HOME}/.local

        echo "Decompress gcc-10.3.1-2023.12-aarch64-linux.tar.gz to ${HOME}/.local"
        tar --no-same-owner -zxf /tmp/devkitdependencies/gcc-10.3.1-2023.12-aarch64-linux.tar.gz -C ${HOME}/.local
        echo "Decompress gcc-10.3.1-2023.12-aarch64-linux.tar.gz to ${HOME}/.local finished."
    fi

    gcc_path=$(which gcc)
    if [[ ${gcc_path} != ${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin/gcc ]]; then
        sed -i '/#*export GCC_HOME=${HOME}\/.local/d' ${HOME}/.bashrc
        sed -i '/#*export PATH=${GCC_HOME}:${PATH}/d' ${HOME}/.bashrc

        echo "change ${HOME}/.bashrc"
        cat >> ${HOME}/.bashrc <<'EOF'
export GCC_HOME=${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin
export PATH=${GCC_HOME}:${PATH}
EOF
        echo "source ${HOME}/.bashrc"
        set +x
        source ${HOME}/.bashrc
    else
        echo "install gcc-10.3.1-2023.12 success."
    fi

else
    echo "Failed to verify the signature of the gcc-10.3.1-2023.12-aarch64-linux.tar.gz installation package."
fi