#!/bin/bash

if [ -e "${HOME}"/.local/devkit_tester ] ; then
  echo "true"
else
  echo "false"
fi