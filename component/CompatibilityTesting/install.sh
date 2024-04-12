#!/bin/bash

set -e


function main (){
  compatibility_test_tar=/tmp/devkitdependencies/compatibility_testing.tar.gz
  echo "Decompress compatibility_testing.tar.gz to ${HOME}/.local"
  tar --no-same-owner -zxf ${compatibility_test_tar} -C ${HOME}/.local/
  echo "Decompress compatibility_testing.tar.gz to ${HOME}/.local finished."
}

main "$@"