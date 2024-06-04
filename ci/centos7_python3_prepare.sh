#!/bin/bash

# 准备centos 构建环境
yum -y install wget.aarch64 gcc.aarch64 gcc-c++.aarch64 libffi-devel.a
# 准备openssl 1.1.1
mkdir /home/package

# shellcheck disable=SC2164
pushd /home/package
export LD_LIBRARY_PATH=/opt/local/lib:$LD_LIBRARY_PATH
export PATH=/opt/local/bin:$PATH

wget https://github.com/Perl/perl5/archive/refs/tags/v5.28.0.tar.gz
tar -xvzf v5.28.0.tar.gz
# shellcheck disable=SC2164
pushd perl5-5.28.0/
./Configure -des -Dprefix=/opt/local/
make -j 16
make install
# shellcheck disable=SC2164
popd

wget https://www.openssl.org/source/old/1.1.1/openssl-1.1.1w.tar.gz
tar -xvzf openssl-1.1.1w.tar.gz
# shellcheck disable=SC2164
pushd openssl-1.1.1w
./config --prefix=/opt/local
make -j 16
make install
# shellcheck disable=SC2164
popd

# 准备python3.9.7
yum -y install libffi-devel.aarch64 bzip2-devel.aarch64 zlib-devel.aarch64 readline-devel gdbm-devel tk-devel.aarch64 uuid.aarch64 sqlite-devel.aarch64 ncurses-devel.aarch64 xz-devel.aarch64
wget https://mirrors.huaweicloud.com/python/3.9.7/Python-3.9.7.tgz
tar -xvzf Python-3.9.7.tgz
# shellcheck disable=SC2164
pushd /home/package/Python-3.9.7
./configure -prefix=/opt/python3 -with-openssl=/opt/local/ -enable-optimizations -enable-shared
make -j 4
make install
# shellcheck disable=SC2164
popd

export LD_LIBRARY_PATH=/opt/python3/lib:/opt/local/lib:$LD_LIBRARY_PATH
export PATH=/opt/python3/bin:/opt/local/bin:$PATH

pip3 config set global.index-url https://mirrors.huaweicloud.com/repository/pypi/simple
pip3 install Pyinstaller
pip3 install urllib3
pip3 install requests
pip3 install paramiko
pip3 install psutil

pip3 install wget
pip3 install pyyaml
pip3 install timeout_decorator

