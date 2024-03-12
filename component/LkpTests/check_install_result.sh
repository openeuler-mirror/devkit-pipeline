#!/bin/bash

lkp_path=$(which lkp)
if [[ "$?" == *"no lkp in "* ]]; then
    echo "false"
else
    echo "true"
fi