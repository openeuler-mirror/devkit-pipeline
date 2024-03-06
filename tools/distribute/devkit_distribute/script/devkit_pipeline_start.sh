#!/bin/bash

# shellcheck disable=SC2164
current_path=$(cd $(dirname "$0"); pwd)

nohup "${current_path}"/entrance "$@" >/dev/null 2>&1 &

echo "start devkit success"