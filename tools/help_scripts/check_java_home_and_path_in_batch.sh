#!/bin/bash
# 检查bishengJDK安装的三个方面
# 1. 检查安装路径是否存在
# 2. 检查JAVA_HOME是否和安装路径一致
# 3. 检查java命令的全路径形式包含安装路径

# 安装的目标位置,即JAVA_HOME
JAVA_HOME_VALUE=/opt/software/bisheng-jdk1.8.0_412
# 哪些服务器需要检查  每行一个服务器 格式：ip password 。其中password可以不写 默认为DEFAULT_PASSWORD
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
    if [[ -z $ip ]];then
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
/usr/bin/expect << EOF
set timeout 10
send_user "$USER@$ip:create dir"
spawn ssh $USER@$ip
expect {
  "*yes/no" {send "yes\r";exp_continue}
  "Permission denied, please try again" {send_user "Permission denied to login user:$USER"; exit 1;}
  "*password" {send "$passwd\r";exp_continue}
  "*Password" {send "$passwd\r";exp_continue}
  "Enter passphrase for key*" {send "$passwd\r";exp_continue}
  "Last login:" {send_user "success to login\n"}
  timeout {send_user "time out to login user:$USER\n"; exit 2}
}

expect -re "$|#" { send "if \[ -d $JAVA_HOME_VALUE \];then echo \"True\"; else echo \"False\";fi\r"}
expect {
  "True" {send_user "success to copy bisheng jdk to server";}
  "False" {send_user "failed to copy bisheng jdk to server";exit 3}
  timeout {send_user "time out to -d"; exit 3}
}

expect -re "$|#" { send "env|grep JAVA_HOME"}
expect {
  "$JAVA_HOME_VALUE" {send_user "success to update JAVA_HOME";}
  timeout {send_user "time out to env JAVA_HOME"; exit 4}
}

expect -re "$|#" { send "which java\r"}
expect {
  "$JAVA_HOME_VALUE" {send_user "success to update PATH";}
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
    echo -e "\033[31m 服务器${ip_arr[$index]} 中 $JAVA_HOME_VALUE 文件夹不存在。 \033[0m"
  elif [[ $ret -eq 4 ]];then
    echo -e "\033[31m 服务器${ip_arr[$index]} 用户${USER}的JAVA_HOME环境变量不是$JAVA_HOME_VALUE。 \033[0m"
  else
    echo -e "\033[31m 服务器${ip_arr[$index]} 直接使用的java命令不是${JAVA_HOME_VALUE}/bin/java。\033[0m"
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