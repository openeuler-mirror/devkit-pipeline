#!/bin/bash
##################################
#功能描述: 使用NMAP进行端口扫描
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-02-10 修改
##################################

if [ ! -d "./log" ]; then
  mkdir ./log
fi
CURRENT_PATH=$(pwd)
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
app_log_file=app_log.log_${current_time}
nmap_log_file=nmap.log_${current_time}

write_messages() {
  # 日志输出函数
  # 参数1：输出日志级别
  # 参数2：输出颜色，0-默认，31-红色，32-绿色，33-黄色，34-蓝色，35-紫色，36-天蓝色，3-白色。
  # 参数3：执行步骤。
  # 参数4：输出的日志内容。
  DATE=$(date "+%Y-%m-%d %H:%M:%S")
  messages=$4
  step=$3
  level_info=$1
  colors=$2
  case $level_info in
  i) echo "#${DATE}#info#${step}#${messages}" >> "${CURRENT_PATH}"/log/"${log_file}"
       ;;
  e) echo "#${DATE}#error#${step}#${messages}" >> "${CURRENT_PATH}"/log/"${log_file}"
     echo -e "\033[1;31m${messages}\033[0m"
    ;;
  m) echo "#${DATE}#value#${step}#${messages}" >> "${CURRENT_PATH}"/log/"${log_file}" ;;
  s) echo "#${DATE}#serious#${step}#${messages}" >> "${CURRENT_PATH}"/log/"${error_file}"
     echo -e "\033[1;31m${messages}\033[0m"
     ;;
  c) echo -e "\033[1;34m${messages}\033[0m"
     echo "#${DATE}#info#${step}#${messages}" >> "${CURRENT_PATH}"/log/"${log_file}"
    ;;
  esac
}

port_scan() {
    # 安全扫描，使用nmap进行端口扫描
    file_path="data/test/safety/"
    if [ ! -d "${file_path}" ]; then
        mkdir -p "${file_path}"
    fi
    write_messages  i 0 6 "现在进行端口安全测试"
    ip_addrs=$(ifconfig -a | grep inet | grep -v '127.0.0.1\|172.17.0.1' | grep -v inet6 \
    | awk '{print $2}' | sed -e 's/addr://g')
    if [ -z "${ip_addrs}" ]; then
        write_messages  e 0 6 "获取主机IP地址出错"
    fi

    for ip in ${ip_addrs}; do
        write_messages  c 34 6 "安全测试采集:执行${ip}的 TCP 端口扫描"
        if ! nmap -sS -A -v --reason -p- -n -Pn -oA ${file_path}"${ip}""tcp" "${ip}" --host-timeout 360\
        >>./log/"${nmap_log_file}" 2>&1; then
            write_messages  e 0 6 "${ip}的TCP 端口扫描出错"
        fi
        write_messages  c 34 6 "安全测试采集:执行${ip}的 UCP 端口扫描"
        if ! nmap -sU -A -v --reason -p- -n -Pn -oA ${file_path}"${ip}""udp" "${ip}" --host-timeout 360\
        >>./log/"${nmap_log_file}" 2>&1; then
            write_messages  e 0 6 "${ip}的UDP 端口扫描出错"
        fi
        write_messages  c 34 6 "安全测试采集:执行${ip}的 protocol扫描"
        if ! nmap -sO -v --reason -n -oA  ${file_path}"${ip}""protocol" "${ip}" --host-timeout 360\
        >>./log/"${nmap_log_file}" 2>&1; then
            write_messages  e 0 6 "${ip}的Protocol 端口扫描出错"
        fi
    done
    write_messages  i 0 6 "端口安全测试结束"
}

port_scan
