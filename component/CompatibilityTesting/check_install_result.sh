#!/bin/bash

compatibility_test_path=$(which compatibility_test)
if [[ ${compatibility_test_path} == ${HOME}/.local/compatibility_testing/bin/compatibility_test ]]; then
    echo "true"
else
    if [[ -f ${HOME}/.local/compatibility_testing/bin/compatibility_test ]] && [[ $(grep '^export PATH=${HOME}/.local/compatibility_testing/bin:${PATH}$' ${HOME}/.bashrc | wc -l) == "1" ]]; then
        echo "true"
    else
        echo "false"
    fi
fi