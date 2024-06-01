#!/bin/bash
##################################
#Function description: Run the NMAP command to scan listening ports
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
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
    # Log output function
    # Parameter 1: specifies a log level.
    # Parameter 2: specifies a color. The value 0 indicates the default color, 31 indicates red, 32 indicates green, 33 indicates yellow, 34 indicates blue, 35 indicates purple, 36 indicates sky blue, and 3 indicates white.
    # Parameter 3: specifies the execution procedure.
    # Parameter 4: specifies the log content.
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
    # Security scan: Run the NMAP command to scan listening ports.
    file_path="data/test/safety/"
    if [ ! -d "${file_path}" ]; then
        mkdir -p "${file_path}"
    fi
    write_messages  i 0 6 "Perform port security test now"
    ip_addrs=$(ifconfig -a | grep inet | grep -v '127.0.0.1\|172.17.0.1' | grep -v inet6 \
    | awk '{print $2}' | sed -e 's/addr://g')
    if [ -z "${ip_addrs}" ]; then
        write_messages  e 0 6 "An error occurred when obtaining the host IP address."
    fi

    for ip in ${ip_addrs}; do
        write_messages  c 34 6 "Security test collection: Scan the TCP port whose IP address is ${ip}."
        if ! nmap -sS -A -v --reason -p- -n -Pn -oA ${file_path}"${ip}""tcp" "${ip}" --host-timeout 360\
        >>./log/"${nmap_log_file}" 2>&1; then
            write_messages  e 0 6 "An error occurred when scanning the TCP port whose IP address is ${ip}."
        fi
        write_messages  c 34 6 "Security test collection: Scan the UDP port whose IP address is ${ip}."
        if ! nmap -sU -A -v --reason -p- -n -Pn -oA ${file_path}"${ip}""udp" "${ip}" --host-timeout 360\
        >>./log/"${nmap_log_file}" 2>&1; then
            write_messages  e 0 6 "An error occurred when scanning the UDP port whose IP address is ${ip}."
        fi
        write_messages  c 34 6 "Security test collection: Scan the protocol port whose IP address is ${ip}."
        if ! nmap -sO -v --reason -n -oA  ${file_path}"${ip}""protocol" "${ip}" --host-timeout 360\
        >>./log/"${nmap_log_file}" 2>&1; then
            write_messages  e 0 6 "An error occurred when scanning the protocol port whose IP address is ${ip}."
        fi
    done
    write_messages  i 0 6 "The port security test is complete."
}

port_scan
