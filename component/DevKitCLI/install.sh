#!/bin/bash
set -e
cd /tmp/devkitdependencies/

function main() {
    mkdir -p ${HOME}/.local
    echo "Decompress DevKit-CLI-24.0.T50-Linux-Kunpeng.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/DevKit-CLI-24.0.T50-Linux-Kunpeng.tar.gz -C ${HOME}/.local
    echo "Decompress DevKit-CLI-24.0.T50-Linux-Kunpeng.tar.gz to ${HOME}/.local finished."

    echo "change ${HOME}/.bashrc"
    sed -i '/DevKit-CLI-24.0.T50-Linux-Kunpeng/d' ${HOME}/.bashrc
    cat >> ${HOME}/.bashrc <<'EOF'
export PATH=${HOME}/.local/DevKit-CLI-24.0.T50-Linux-Kunpeng:${PATH}
EOF

    echo "source ${HOME}/.bashrc"
    set +x
    source ${HOME}/.bashrc
}

main "$@"