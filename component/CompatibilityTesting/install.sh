#!/bin/bash

set -e


function main (){
  compatibility_test_tar=$1
  echo "Decompress compatibility_testing.tar.gz to ${HOME}/.local"
  tar -zvxf ${compatibility_test_tar} -C ${HOME}/.local/
  echo "Decompress compatibility_testing.tar.gz to ${HOME}/.local finished."
}

main "$@"