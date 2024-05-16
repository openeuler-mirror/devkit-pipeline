#!/bin/bash

set -e


function main (){
  mkdir -p "${HOME}"/.local/
  echo "Decompress devkit_tester.tar.gz to ${HOME}/.local/"
  tar --no-same-owner -zxf /tmp/devkitdependencies/devkit_tester.tar.gz -C ${HOME}/.local
  echo "Decompress devkit_tester.tar.gz to ${HOME}/.local finished."
}


main "$@"