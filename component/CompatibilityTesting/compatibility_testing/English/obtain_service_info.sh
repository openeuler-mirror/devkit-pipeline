#!/bin/bash
##################################
#Function description: Obtain the hardware configuration of the test environment, such as the server model, drive version,
# kernel version, and information about the PCI and CPU.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-03-02
##################################

if [[ ! -d "./log" ]]; then
  mkdir ./log
fi
CURRENT_PATH=$(pwd)
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
app_log_file=app_log.log_${current_time}

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

get_service_info() {
    # Obtain Server Info
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
    command_desc=("Server Model" "pci Info" "CPU Info" "Partition Drive" "Kernel Info")
    length=${#file_name_list[@]}
    for ((i = 0; i < "${length}"; i++)); do
        file_name=${file_name_list[$i]}
        comm=${command_list[$i]}
        desc=${command_desc[$i]}
        if ! eval "${comm}" >"${file_name}"; then
            write_messages   e 0 4 "Failed to obtain ${desc} by invoking the ${comm} command."
        else
            write_messages  i 0 4 "Succeed to obtain ${desc} by invoking the ${comm} command."
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





