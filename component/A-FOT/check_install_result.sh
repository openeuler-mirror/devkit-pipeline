#!/bin/bash

a_fot_path=$(which a-fot)
if [[ ${a_fot_path} == ${HOME}/.local/a-fot/a-fot ]]; then
    echo "true"
else
    if [[ -f ${HOME}/.local/a-fot/a-fot ]] && [[ $(grep -A1 '^export A_FOT_HOME=${HOME}/.local/a-fot$' ${HOME}/.bashrc | grep '^export PATH=${A_FOT_HOME}:${PATH}$' | wc -l) == "1" ]]; then
        echo "true"
    else
        echo "false"
    fi
fi
