#!/bin/bash

if [ -e "${HOME}"/.local/lkp-tests/programs/devkit_distribute ] ; then
  echo "true"
else
  echo "false"
fi