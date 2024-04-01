#!/bin/bash

sudo yum install tar perf git wget rubygems -y >/dev/null 2>&1
if [[ "$?" == 0 ]]; then
    echo "true"
else
    echo "false"
fi