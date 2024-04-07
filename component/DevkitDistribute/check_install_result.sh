#!/bin/bash

if [ -e "${HOME}"/.local/devkit_distribute ] ; then
  echo "true"
else
  echo "false"
fi