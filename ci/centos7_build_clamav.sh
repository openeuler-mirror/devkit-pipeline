#!/bin/bash

yum install -y \
  gcc gcc-c++ make python3 python3-pip valgrind expect \
  bzip2-devel check-devel json-c-devel libcurl-devel libxml2-devel \
  ncurses-devel openssl-devel pcre2-devel sendmail-devel zlib-devel

# shellcheck disable=SC2024
curl --proto '=https' --tlsv1.2 https://sh.rustup.rs -sSf > rust.sh
chmod +x rust.sh

# shellcheck disable=SC2121
expect RUSTUP_DIST_SERVER=https://mirrors.ustc.edu.cn/rust-static
# shellcheck disable=SC2121
expect RUSTUP_UPDATE_ROOT=https://mirrors.ustc.edu.cn/rust-static/rustup

# bash rust.sh
bash expect/install_rust.sh
# shellcheck disable=SC1090
source ~/.bashrc


wget https://github.com/Kitware/CMake/archive/refs/tags/v3.29.3.tar.gz
tar -xvzf v3.29.3.tar.gz

# shellcheck disable=SC2164
pushd "CMake-3.29.3"
./configure --prefix=/opt/local
make
make install

export LD_LIBRARY_PATH=/opt/local/lib:$LD_LIBRARY_PATH
export PATH=/opt/local/bin:$PATH

wget https://clamav-site.s3.amazonaws.com/production/release_files/files/000/001/607/original/clamav-1.3.1.tar.gz
tar -xvzf clamav-1.3.1.tar.gz
cmake -DCMAKE_INSTALL_PREFIX=/opt/clamav-1.3.1   ..
make -j
make install
cp /opt/clamav-1.3.1/etc/clamd.conf.sample /opt/clamav-1.3.1/etc/clamd.conf
cp /opt/clamav-1.3.1/etc/freshclam.conf.sample /opt/clamav-1.3.1/etc/freshclam.conf
# 注释两个文件中的example

