#!/bin/bash

lkp_path=$(which lkp)
if [[ "${lkp_path}" == *"/bin/lkp"* ]]; then
    echo "true"
else
    echo "false, 请确认是否配everything的yum源，以及是否给安装用户配属sudo权限"
fi