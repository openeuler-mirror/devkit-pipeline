#!/bin/bash

# 安装的目标位置，当前最终安装在/opt/software/bisheng-jdk1.8.0_402
TARGET_DIR=/opt/software/bisheng-jdk1.8.0_402
# 哪些服务器需要安装  每行一个服务器 格式：ip password 。其中password可以不写 默认为DEFAULT_PASSWORD
IP_FILE=ip.all
# 服务器的登陆用户
USER=root
# 默认密码，在IP_FILE未进行配置的服务器使用的密码
DEFAULT_PASSWORD="Huawei123"

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

function connect_host_and_check_bisheng_jdk() {
  local ip=$1
  local passwd=$2
/usr/bin/expect > /dev/null << EOF
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

expect -re "$|#" { send "ls  $TARGET_DIR\r"}
expect {
  "No such file" {send_user "success to update JAVA_HOME";exit 3}
  "$TARGET_DIR" {send_user "success to copy bisheng jdk to server";}
  timeout {send_user "time out to ls"; exit 3}
}

expect -re "$|#" { send "echo  \$JAVA_HOME\r"}
expect {
  "$TARGET_DIR" {send_user "success to update JAVA_HOME";}
  timeout {send_user "time out to echo JAVA_HOME"; exit 4}
}

expect -re "$|#" { send "which java\r"}
expect {
  "$TARGET_DIR" {send_user "success to login";}
  timeout {send_user "time out to which java"; exit 5}
}
expect -re "$|#" { send "logout\r"}
expect eof
EOF
}

function print_result() {
  local index=$1
  local ret=$2
  if [[ $ret -eq 0 ]];then
    echo -e "\033[32m 服务器${ip_arr[$index]} 安装成功，且修改的环境变量(JAVA_HOME)和PATH在用户${USER}生效。\033[0m"
  elif [[ $ret -eq 1 ]];then
    echo -e "\033[31m 服务器${ip_arr[$index]} 密码错误。\033[0m"
  elif [[ $ret -eq 2 ]];then
    echo -e "\033[31m 服务器${ip_arr[$index]} 登陆超时。可能原因：服务器地址不正确或者登陆返回不包含Last login或者缺少命令  \033[0m"
  elif [[ $ret -eq 3 ]];then
    echo -e "\033[31m 服务器${ip_arr[$index]} 不存在文件夹$TARGET_DIR。 \033[0m"
  elif [[ $ret -eq 4 ]];then
    echo -e "\033[31m 服务器${ip_arr[$index]} 用户${USER}的环境变量JAVA_HOME不是$TARGET_DIR。 \033[0m"
  else
    echo -e "\033[31m 服务器${ip_arr[$index]} 直接使用java命令不是${TARGET_DIR}/bin/java。\033[0m"
  fi
}

function check_all_host() {
  for ((i=0;i<${#ip_arr[@]};i++))
  do
    connect_host_and_check_bisheng_jdk "${ip_arr[$i]}" ${passwd_arr[$i]}
    print_result $i $?
  done
}


function main() {
  initialize_arr
  check_all_host
}

main