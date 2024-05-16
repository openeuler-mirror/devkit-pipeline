#!/bin/bash

set -e
current_dir=$(cd $(dirname "$0"); pwd)

echo "${current_dir}"

pushd "$current_dir"

mvn clean package

popd