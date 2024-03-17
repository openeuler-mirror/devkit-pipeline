#!/bin/bash

set -e


function main (){
  devkit_distribute_tar=$1
  echo "Decompress devkit_distribute.tar.gz to ${HOME}/.local/lkp-tests/programs"
  tar -zvxf "${devkit_distribute_tar}" -C /"${HOME}"/.local/lkp-tests/programs
  chmod 755 "${HOME}"/.local/lkp-tests/programs/devkit_distribute/bin/start.sh
  ln -s "${HOME}"/.local/lkp-tests/programs/devkit_distribute/bin/start.sh "${HOME}"/.local/lkp-tests/tests/devkit_distribute
  echo "Decompress devkit_distribute.tar.gz to ${HOME}/.local finished."
}


main "$@"