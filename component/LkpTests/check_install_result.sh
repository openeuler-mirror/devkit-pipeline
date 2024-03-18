#!/bin/bash

lkp_path=$(which lkp)
if [[ "$?" == *"/bin/lkp"* ]]; then
    echo "true"
else
    echo "flase"
fi