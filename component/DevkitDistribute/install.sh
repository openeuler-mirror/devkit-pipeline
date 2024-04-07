#!/bin/bash

set -e


function main (){
  echo "Decompress devkit_distribute.tar.gz to ${HOME}/.local/"
  tar --no-same-owner -zxf /tmp/devkitdependencies/devkit_distribute.tar.gz -C ${HOME}/.local
  echo "Decompress devkit_distribute.tar.gz to ${HOME}/.local finished."
}


main "$@"