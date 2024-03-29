#!/bin/bash

cd /tmp/devkitdependencies/
verify_signature=$(sha256sum -c BiShengCompiler-3.2.0-aarch64-linux.tar.gz.sha256 >/dev/null 2>&1; echo $?)
if [[ ${verify_signature} -eq "0" ]]; then
    if [[ ! -d ${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux ]]; then
        mkdir -p ${HOME}/.local

        echo "Decompress BiShengCompiler-3.2.0-aarch64-linux.tar.gz to ${HOME}/.local"
        tar -zxf /tmp/devkitdependencies/BiShengCompiler-3.2.0-aarch64-linux.tar.gz -C ${HOME}/.local
        echo "Decompress BiShengCompiler-3.2.0-aarch64-linux.tar.gz to ${HOME}/.local finished."
    fi

    clang_path=$(which clang)
    if [[ ${clang_path} != ${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux/bin/clang ]]; then
        sed -i '/#*export BISHENG_COMPILER_HOME=${HOME}\/.local/d' ${HOME}/.bashrc
        sed -i '/#*export PATH=${BISHENG_COMPILER_HOME}:${PATH}/d' ${HOME}/.bashrc

        echo "change ${HOME}/.bashrc"
        cat >> ${HOME}/.bashrc <<'EOF'
export BISHENG_COMPILER_HOME=${HOME}/.local/BiShengCompiler-3.2.0-aarch64-linux/bin
export PATH=${BISHENG_COMPILER_HOME}:${PATH}
EOF
        echo "source ${HOME}/.bashrc"
        set +x
        source ${HOME}/.bashrc
    else
        echo "install BiShengCompiler-3.2.0 success."
    fi

else
    echo "Failed to verify the signature of the BiShengCompiler-3.2.0-aarch64-linux.tar.gz installation package."
fi