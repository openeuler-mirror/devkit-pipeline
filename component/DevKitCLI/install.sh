#!/bin/bash
set -e
cd /tmp/devkitdependencies/

mkdir -p ${HOME}/.local
echo "Decompress DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz to ${HOME}/.local"
tar --no-same-owner -zxf /tmp/devkitdependencies/DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz -C ${HOME}/.local
echo "Decompress DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz to ${HOME}/.local finished."
sudo ln -s ${HOME}/.local/DevKit-CLI-24.0.RC1-Linux-Kunpeng/devkit /usr/local/bin
echo "create DevkitCLI soft-link success."
