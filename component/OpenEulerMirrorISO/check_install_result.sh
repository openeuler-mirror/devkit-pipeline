#!/bin/bash

yum install git -y >/dev/null 2>&1
if [[ "$?" == 0 ]]; then
    echo "true"
else
    echo "false"
fi