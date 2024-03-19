#!/bin/bash

lkp_path=$(which lkp)
if [[ "${lkp_path}" == *"/bin/lkp"* ]]; then
    echo "true"
else
    echo "false"
fi