#!/bin/bash


bash centos7_python3_prepare.sh

# 准备centos 构建环境
yum -y install git.aarch64 vim.aarch64 java-11-openjdk-devel.aarch64

wget https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

tar -xvzf apache-maven-3.9.6-bin.tar.gz -C /opt --no-same-owner

export PATH=/opt/apache-maven-3.9.6/bin:$PATH


