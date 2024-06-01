#!/bin/bash
##################################
#Function description: Collect indicator data related to the CPU, memory, drive, NIC, and power consumption.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
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

get_performance() {
    # Collect indicator data related to the CPU, memory, drive, NIC, and power consumption.
    # Parameter 1: collection interval
    # Parameter 2: collection duration
    # Parameter 3: collection sequence. The value 0 indicates collection before the compatibility test, 1 indicates collection during the performance test, and 2 indicates collection after the compatibility test
    # Parameter 4: step
    frequency=$1
    seq=$3
    during_time=$2
    step=$4
    times=$((during_time * 60 / frequency))
    power_path="data/test/power/"
    if [[ ! -d "${power_path}" ]]; then
        mkdir -p "${power_path}"
    fi
    if [[ "${seq}" -eq 1 ]]; then
        file_path="data/test/performance/"
        file_seq=1
    elif [[ "${seq}" -eq 0 ]]; then
        file_path="data/test/compatiable/"
        file_seq=0
    else
        file_path="data/test/compatiable/"
        file_seq=1
    fi
    desc_array=('Collect data before the compatibility test' 'Collect data during the performance test' 'Collect data after the compatibility test')
    if [[ ! -d "${file_path}" ]]; then
        mkdir -p "${file_path}"
    fi
    file_name="test_perf_cpu_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:Run the sar -u ${frequency} ${times} command to collect indicator data related to the CPU."

    if ! sar -u "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"  "Failed to collect indicator data related to the CPU by invoking the sar -u ${frequency} ${times} command."
        write_messages  s 0 "${step}"  "${desc_array[${seq}]} Failed to collect indicator data related to the CPU by invoking the sar -u ${frequency} ${times} command."
    fi
    file_name="test_perf_mem_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:Run the sar -r ${frequency} ${times} command to collect indicator data related to the Memory."
    if ! sar -r "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"  "Failed to collect indicator data related to the Memory by invoking the sar -r ${frequency} ${times} command."
        write_messages  s 0 "${step}"  "${desc_array[${seq}]}Failed to collect indicator data related to the Memory by invoking the sar -r ${frequency} ${times} command."
    fi
    file_name="test_perf_net_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:Run the sar -n DEV ${frequency} ${times} command to collect indicator data related to the NIC."
    if ! sar -n DEV "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"   "Failed to collect indicator data related to the NIC by invoking the  sar -n DEV ${frequency} ${times} command."
        write_messages  s 0 "${step}"   "${desc_array[${seq}]}Failed to collect indicator data related to the NIC by invoking the  sar -n DEV ${frequency} ${times} command."
    fi
    file_name="test_perf_disk_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:Run the sar -d -p ${frequency} ${times} command to collect indicator data related to the drive."
    if ! sar -d -p "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"  "Failed to collect indicator data related to the drive by invoking the sar -d -p ${frequency} ${times} command."
        write_messages  s 0  "${step}" "${desc_array[${seq}]}Failed to collect indicator data related to the drive by invoking the sar -d -p ${frequency} ${times} command."
    fi
    if [[ "${seq}" -ne 1 ]];then
        write_messages  i 0 "${step}" "${desc_array[${seq}]}:run ipmitool command to collect indicator data related to power consumption."
        file_name="test_power_${file_seq}.log"
        times=$((during_time * 60 / frequency))
        while [[ ${times} -gt 0 ]];
        do
            if ! ipmitool -I open sensor get 'Power'|grep 'Sensor Reading '|awk '{print $4}' \
            >>"${power_path}""${file_name}"; then
            write_messages   e 0 "${step}" "ailed to collect indicator data related to the power consumption by ipmitool -I open sensor get 'Power' command."
        fi
        sleep "${frequency}"
        times=$((times-1))
        done
        write_messages  i 0 "${step}" "Finish power consumption test."
    fi
    write_messages  i 0 "${step}" "${desc_array[${seq}]} has been finished."
}

get_performance $1 $2 $3 $4