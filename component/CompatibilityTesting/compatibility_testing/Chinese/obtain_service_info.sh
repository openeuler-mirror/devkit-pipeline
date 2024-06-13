#!/bin/bash
##################################
#功能描述: 获取测试环境的硬件配置（服务器型号、硬盘版本、pci信息、CPU信息、内核版本）
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-03-01 修改
##################################
# 创建日志目录

if [[ ! -d "./log" ]]; then
  mkdir ./log
fi
CURRENT_PATH=$(pwd)
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
app_log_file=app_log.log_${current_time}

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

get_service_info() {
    #获取服务器信息
    path="data/hardware/"
    if [[ ! -d "${path}" ]]; then
        mkdir -p "${path}"
    fi
    path="data/software/"
    if [[ ! -d "${path}" ]]; then
        mkdir -p "${path}"
    fi
    file_name_list=("data/hardware/hardware_info.log" "data/hardware/hardware_pcie.log" "data/hardware/hardware_cpu.log"
      "data/hardware/hardware_disk.log" "data/software/system_version.log")
    command_list=("dmidecode" "lspci -nnvv" "lscpu" "lsblk" "cat /proc/version")
    command_desc=("服务器型号" "pci信息" "CPU信息" "硬盘分区" "内核信息")
    length=${#file_name_list[@]}
    for ((i = 0; i < "${length}"; i++)); do
        file_name=${file_name_list[$i]}
        comm=${command_list[$i]}
        desc=${command_desc[$i]}
        if ! eval "${comm}" >"${file_name}"; then
            write_messages   e 0 4 "调用命令${comm}获取${desc}失败"
        else
            write_messages  i 0 4 "调用命令${comm}获取${desc}完成"
        fi
    done

    if hash smartctl 2>/dev/null ; then
        smatrt_file="data/hardware/hardware_smartctl.log"
        OLD_IFS="${IFS}"
        IFS=$'\n'
        for device in $(smartctl --scan|awk -F"#"  '{print $1}');
        do
            smartctl_cmd="smartctl -a ${device}"
            eval "${smartctl_cmd}" >> "${smatrt_file}" 2>> /dev/null;
        done
        IFS="${OLD_IFS}"
    fi


    gcc_file="data/software/system_version.log"
    if hash gcc 2>/dev/null ; then
        gcc_file="data/software/system_version.log"
        echo -e "\n=============gcc=============" >>${gcc_file} 2>> /dev/null;
        gcc --version >> ${gcc_file} 2>> /dev/null;
    fi

    if hash clang 2>/dev/null ; then
        echo -e "\n=============clang=============" >>${gcc_file} 2>> /dev/null;
        clang --version >> ${gcc_file} 2>> /dev/null;
    fi

    if hash java 2>/dev/null ; then
        echo -e "\n=============java=============" >>${gcc_file} 2>> /dev/null;
        java -version >> ${gcc_file} 2>> /dev/null 2>&1;
    fi
}


get_service_info





