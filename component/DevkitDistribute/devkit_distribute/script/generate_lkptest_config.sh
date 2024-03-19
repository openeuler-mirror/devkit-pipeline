#!/bin/bash
# SourceCode build script
# Copyright: Copyright (c) Huawei Technologies Co., Ltd. All rights reserved.

set -ex
current_dir=$(cd $(dirname "$0"); pwd)
root_path=$(realpath "${current_dir}/..")
umask 077
cp "${root_path}/config/devkit_distribute_template.yaml" "${root_path}/config/devkit_distribute.yaml"

function main() {

local ips_list=""
local user="root"
local port=22
local pkey_file=""
local pkey_password=""
local devkit_ip=""
local devkit_port=8086
local devkit_user="devadmin"
local devkit_password="admin100"
local applications=""
local duration=10
local git_path=""

while getopts "i:u:f:a:d:D:g:" opts; do
	case $opts in
	  i)
			ips_list=$OPTARG ;;
	  u)
			user=$OPTARG ;;
		f)
			pkey_file=$OPTARG ;;
	  a)
			applications=$OPTARG ;;
	  d)
			duration=$OPTARG ;;
		D)
			devkit_ip=$OPTARG ;;
		g)
			git_path=$OPTARG ;;
		?)
		  echo "not recogize paramters";;
	esac
done

sed -i "s?\${root_path}?${root_path}?g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${ips_list}/${ips_list}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${user}/${user}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${port}/${port}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s?\${pkey_file}?${pkey_file}?g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${pkey_password}/${pkey_password}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${devkit_ip}/${devkit_ip}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${devkit_port}/${devkit_port}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${devkit_user}/${devkit_user}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${devkit_password}/${devkit_password}/g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s?\${applications}?${applications}?g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s?\${git_path}?${git_path}?g" "${root_path}/config/devkit_distribute.yaml"
sed -i "s/\${duration}/${duration}/g" "${root_path}/config/devkit_distribute.yaml"

}

main "$@"






