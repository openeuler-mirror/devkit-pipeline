#!/bin/bash

cd /tmp/devkitdependencies/


if [[ ! -d ${HOME}/.local/a-fot ]]; then
    mkdir -p ${HOME}/.local

    echo "Decompress a-fot.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf /tmp/devkitdependencies/a-fot.tar.gz -C ${HOME}/.local
    chmod 750 -R ${HOME}/.local/a-fot
    echo "Decompress a-fot.tar.gz to ${HOME}/.local finished."
fi

a_fot_path=$(which a-fot)
if [[ ${a_fot_path} != ${HOME}/.local/a-fot/a-fot ]]; then
    sed -i '/#*export A_FOT_HOME=${HOME}\/.local/d' ${HOME}/.bashrc
    sed -i '/#*export PATH=${A_FOT_HOME}:${PATH}/d' ${HOME}/.bashrc

    echo "change ${HOME}/.bashrc"
    cat >> ${HOME}/.bashrc <<'EOF'
export A_FOT_HOME=${HOME}/.local/a-fot
export PATH=${A_FOT_HOME}:${PATH}
EOF
    echo "source ${HOME}/.bashrc"
    set +x
    source ${HOME}/.bashrc
else
    echo "install a-fot success."
fi
