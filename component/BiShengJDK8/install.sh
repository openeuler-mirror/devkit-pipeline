#!/bin/bash

cd /tmp/devkitdependencies/
verify_signature=$(sha256sum -c bisheng-jdk-8u402-linux-aarch64.tar.gz.sha256 >/dev/null 2>&1; echo $?)
if [[ ${verify_signature} -eq "0" ]]; then
    if [[ ! -d ${HOME}/.local/bisheng-jdk1.8.0_402 ]]; then
        mkdir -p ${HOME}/.local

        echo "Decompress bisheng-jdk-8u402-linux-aarch64.tar.gz to ${HOME}/.local"
        tar -zxf /tmp/devkitdependencies/bisheng-jdk-8u402-linux-aarch64.tar.gz -C ${HOME}/.local
        echo "Decompress bisheng-jdk-8u402-linux-aarch64.tar.gz to ${HOME}/.local finished."
    fi

    java_path=$(which java)
    if [[ ${java_path} != ${HOME}/.local/bisheng-jdk1.8.0_402/bin/java ]]; then
        # 配置alternatives
        echo "do update-alternatives install something to /usr/bin/"
        for BinFilePath in ${HOME}/.local/bisheng-jdk1.8.0_402/bin/*; do
            if [[ -x ${BinFilePath} ]]; then
                BinFileName=$(basename ${BinFilePath})
                sudo update-alternatives --install /usr/bin/${BinFileName} ${BinFileName} ${BinFilePath} 100
            fi
        done
    else
        echo "install bisheng-jdk-8u402 success."
    fi

else
    echo "Failed to verify the signature of the bisheng-jdk-8u402-linux-aarch64.tar.gz installation package."
fi
