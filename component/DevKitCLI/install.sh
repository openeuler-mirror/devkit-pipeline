#!/bin/bash
cd /tmp/devkitdependencies/
echo "Decompress DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz to ${HOME}/.local"
tar -zxf /tmp/devkitdependencies/DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz -C ${HOME}/.local
echo "Decompress DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz to ${HOME}/.local finished."
ln -s ${HOME}/.local/DevKit-CLI-24.0.RC1-Linux-Kunpeng/devkit /usr/local/bin
echo "install DevkitCLI success."
