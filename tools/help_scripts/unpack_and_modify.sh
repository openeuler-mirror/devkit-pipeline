#!/bin/bash

set -e
# bishengJDK的文件名称
BISHENG_JDK_TAR=$1
# bishengJDK解压后的文件夹名称
BISHENG_DIR=$2
# 安装的目标位置，当前最终安装在/opt/software/bisheng-jdk1.8.0_412
TARGET_DIR=$3

# 在/etc/profile 中追加的内容
ADD_JAVA_HOME="export JAVA_HOME=${TARGET_DIR}/${BISHENG_DIR}"

tar --no-same-owner -xzf $TARGET_DIR/$BISHENG_JDK_TAR -C ${TARGET_DIR}
chmod -R 755 $TARGET_DIR

echo $ADD_JAVA_HOME >>/etc/profile

echo 'export PATH=${JAVA_HOME}/bin:${PATH}' >>/etc/profile

echo "success"