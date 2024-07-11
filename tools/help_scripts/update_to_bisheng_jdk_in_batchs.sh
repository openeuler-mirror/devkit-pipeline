#!/bin/bash

# 存放bishengJDK的目录
BISHENG_TAR_DIR=/home/zpp
# bishengJDK的文件名称
BISHENG_JDK_TAR=bisheng-jdk-8u402-linux-aarch64.tar.gz
# bishengJDK解压后的文件夹名称
BISHENG_DIR=bisheng-jdk1.8.0_402
# 安装的目标位置，当前最终安装在/opt/software/bisheng-jdk1.8.0_402
TARGET_DIR=/opt/software
# 哪些服务器需要安装  每行一个服务器 格式：ip password 。其中password可以不写 默认为DEFAULT_PASSWORD
IP_FILE=/home/zpp/ip.all
# 服务器的登陆用户
USER=root
# 默认密码，在IP_FILE未进行配置的服务器使用的密码
DEFAULT_PASSWORD="Huawei123"
# 并行度，同时进行安装的服务器个数
PARALLEL=10

# 在/etc/profile 中追加的内容
ADD_JAVA_HOME="export JAVA_HOME=${TARGET_DIR}/${BISHENG_DIR}"
ADD_PATH="export PATH=\${JAVA_HOME}/bin:\${PATH}"

declare -A ip_arr
declare -A passwd_arr

function initialize_arr() {
  local index=0
  #
  while read line || [[ -n ${line} ]]
  do
    local ip=`echo "${line}"|awk '{print $1}'`
    if [[ -n $ip ]];then
      continue
    fi
    local password=`echo "${line}"|awk '{print $2}'`
    #
    if [[ -z $password ]];then
      password=$DEFAULT_PASSWORD
    fi
    ip_arr[$index]=$ip
    passwd_arr[$index]=$password
    index=`expr $index + 1`
  done < ${IP_FILE}
}

function connect_host_and_update_to_bisheng_jdk() {
  local ip=$1
  local passwd=$2
/usr/bin/expect << EOF
set timeout 10
send_user "$USER@$ip:create dir"
spawn ssh $USER@Sip
expect {
  "*yes/no" {send "yes\r";exp_continue}
  "*password" {send "$passwd\r";exp_continue}
  "*Password" {send "$passwd\r";exp_continue}
  "Enter passphrase for key*" {send "$passwd\r";exp_continue}
  "Permission denied, please try again" {send_user "Permission denied to login user:$USER"; exit 1;}
  "Last login:" {send_user "success to login"}
  timeout {send_user "time out to login user:$USER"; exit 2}
}
expect -re "$|#" { send "mkdir -p $TARGET_DIR\r"}
expect -re "$|#" { send "logout\r"}

expect -re "$|#"
spawn scp -r $BISHENG_TAR_DIR/$BISHENG_JDK_TAR  $USER@Sip:$TARGET_DIR
expect{
  "*yes/no" {send "yes\r";exp_continue}
  "*password" {send "$passwd\r";}
  "*Password" {send "$passwd\r";}
  "Enter passphrase for key*" {send "$passwd\r";}
  -re "$|#" {send_user "success to copy file\n"}
}

expect -re "$|#"
spawn ssh $USER@Sip
expect {
  "*yes/no" {send "yes\r";exp_continue}
  "*password" {send "$passwd\r";}
  "*Password" {send "$passwd\r";}
  "Enter passphrase for key*" {send "$passwd\r";}
  "Last login:" {send_user "success to login"}
}
expect -re "$|#" { send "tar --no-same-owner -xzf $TARGET_DIR/$BISHENG_JDK_TAR -C ${TARGET_DIR}\r"}
expect -re "$|#" { send "rm -rf $TARGET_DIR/$BISHENG_JDK_TAR\r"}
expect -re "$|#" { send "chmod -R 755 $TARGET_DIR\r"}
expect -re "$|#" { send "echo $ADD_JAVA_HOME >>/etc/profile\r"}
expect -re "$|#" { send "echo '$ADD_PATH' >>/etc/profile\r"}
expect -re "$|#" { send "logout\r"}
expect eof
EOF
}

function print_result() {
  local index=$1
  local ret=$2
  if [[ $ret -eq 0 ]];then
    echo -e "\033[32m 服务器${ip_arr[$index]}安装成功。安装位置为$TARGET_DIR,并在/etc/profile文件中追加了以下配置：\n ${ADD_JAVA_HOME}\n${ADD_PATH} \033[0m"
  elif [[ $ret -eq 1 ]];then
    echo -e "\033[31m 服务器${ip_arr[$index]} 密码错误。\033[0m"
  else
    echo -e "\033[31m 服务器${ip_arr[$index]} 未安装成功。可能原因：服务器地址不正确或者登陆返回不包含Last login或者缺少命令  \033[0m"
  fi
}

function update_all_host() {
  local arr_length=${#ip_arr[@]}
  if [ $arr_length -gt $PARALLEL ];then
    for ((i=0;i<${PARALLEL};i++))
    do
      {
        for ((j=i;j<${arr_length};j=j+$PARALLEL))
        do
          connect_host_and_update_to_bisheng_jdk "${ip_arr[$j]}" ${passwd_arr[$j]}
          print_result $j $?
        done
      } &
    done
    wait
  else
    for ((i=0;i<${arr_length};i++))
    do
      {
        connect_host_and_update_to_bisheng_jdk "${ip_arr[$i]}" ${passwd_arr[$i]}
        print_result $i $?
      } &
    done
    wait
  fi

}


function main() {
  initialize_arr
  update_all_host
}

main