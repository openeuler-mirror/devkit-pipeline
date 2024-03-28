#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

# shellcheck disable=SC2164
current_dir=$(cd $(dirname "$0"); pwd)
root_path=$(realpath "${current_dir}/..")

if [[ ! -e "${root_path}/data/result" ]] ; then
  eixt -1
fi

# shellcheck disable=SC2013
# shellcheck disable=SC2006
for line in `cat "${root_path}"/data/result`
do
  if [[ $line == "0" ]]; then
    exit 0
  else
    exit "$line"
  fi
done
