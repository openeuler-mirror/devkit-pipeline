#!/bin/bash

set -e


function main (){
    compatibility_test_tar=/tmp/devkitdependencies/compatibility_testing.tar.gz
    echo "Decompress compatibility_testing.tar.gz to ${HOME}/.local"
    tar --no-same-owner -zxf ${compatibility_test_tar} -C ${HOME}/.local/
    echo "Decompress compatibility_testing.tar.gz to ${HOME}/.local finished."

    echo "change ${HOME}/.bashrc"
    sed -i '/compatibility_testing/d' ${HOME}/.bashrc
    echo 'export PATH=${HOME}/.local/compatibility_testing/bin:${PATH}' >> ${HOME}/.bashrc

    echo "source ${HOME}/.bashrc"
    set +x
    source ${HOME}/.bashrc
}

main "$@"