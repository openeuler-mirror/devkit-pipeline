#!/bin/bash
##################################
#Function description: Check whether the usage of the CPU, memory, drive and NIC is too high. If the usage is too high,
# a message will be displayed indicating that the current environment is not idle.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
##################################


# Create a log directory.
if [[ ! -d "./log" ]]; then
    mkdir ./log
fi
CURRENT_PATH=$(pwd)
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
app_log_file=app_log.log_${current_time}
# marked idle
CPU_NOT_IDLE=0
MEM_NOT_IDLE=0
NET_NOT_IDLE=0
DISK_NOT_IDLE=0

# maximum check times
MAX_CPU_IDLE=10.00
MAX_MEM_IDLE=5.00
MAX_DISK_IDLE=5.00
MAX_NET_CONNS=100

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

sys_env_inspectation() {
    # Environment self-check: Check whether the usage of the CPU, memory, drive and NIC is too high.
    # script PID
    SCRIPTS_PID=$(ps -ef | grep -v grep | grep "$0" | awk '{print $2}')

    DISK_NAME=$(iostat -d -x |sed -n '4,$p'|wc -l)
    IOS_LINE=$(( ( DISK_NAME + 1 )*4 +2 ))

    CPU_IDLE=$(top -n 1 -b | head -10 | tail -3 | awk '{if ($12 !~/top/ && $1!~/"${SCRIPTS_PID}"/) print $9}' | head -1)
    write_messages  i 0 3 "Environment self-check: CPU used ${CPU_IDLE}"
    if [[ -z "${CPU_IDLE}" ]]; then
        write_messages   e 0 3 "Environment self-check: Check whether an error occurs when querying the CPU usage or invoking the top command."
    elif [[ "$(echo "${CPU_IDLE}>${MAX_CPU_IDLE}" | bc)" -eq 1 ]]; then
        write_messages   e 0 3 "Environment self-check: When detecting that the CPU usage ${CPU_IDLE} of the application exceeds the threshold, run the top command to check the usage."
        CPU_NOT_IDLE=1
    else
        CPU_NOT_IDLE=0
    fi
    MEM_IDLE=$(top -n 1 -b | head -10 | tail -3 | awk '{if ($12 !~/top/ && $1!~/"${SCRIPTS_PID}"/) print $10}' | head -1)
    write_messages  i 0 3 "Environment self-check: memory used ${MEM_IDLE}"
    if [[ -z "${MEM_IDLE}" ]]; then
        write_messages   e 0 3 "Environment self-check: Check whether an error occurs when querying the memory usage or invoking the top command."
    elif [[ "$(echo "${MEM_IDLE}>${MAX_MEM_IDLE}" | bc)" -eq 1 ]]; then
        write_messages   e 0 3 "Environment self-check: When detecting that the memory usage ${MEM_IDLE} of the application exceeds the threshold, run the top command to check the usage."
        MEM_NOT_IDLE=1
    else
        MEM_NOT_IDLE=0
    fi
    # run the iostat command to detect the bandwidth of the drive
    DISK_IDLE=$(iostat -d -x 1 5 |sed -n ''${IOS_LINE}',$p' |grep -v Device|awk '{print $NF}'|sort -nr|head -1)
    write_messages  i 0 3 "Environment self-check: drive used ${DISK_IDLE}"
    if [[ -z "${DISK_IDLE}" ]]; then
        write_messages   e 0 3 "Environment self-check: Check whether an error occurs when querying the drive usage or invoking the iostat command."
    elif [[ "$(echo "${DISK_IDLE}>${MAX_DISK_IDLE}" | bc)" -eq 1 ]]; then
        write_messages   e 0 3 "Environment self-check: When detecting that the bandwidth usage ${DISK_IDLE} of the drive exceeds the threshold, run the iostat -d -x command to check the usage."
        DISK_NOT_IDLE=1
    else
        DISK_NOT_IDLE=0
    fi
    # Number of detected network connections
    NET_CONNECTIONS=$(netstat -n | awk '/^tcp/ {++S[$NF]}END{for(a in S) print S[a]}' | sort -nr | head -1)
    write_messages  i 0 3 "Environment self-check: Number of network connections ${NET_CONNECTIONS}"
    if [[ -z "${NET_CONNECTIONS}" ]]; then
        write_messages   e 0 3 "Environment self-check: Check whether an error occurs when querying the number of network connections or invoking the netstat command."
    elif [[ "${NET_CONNECTIONS}" -ge "${MAX_NET_CONNS}" ]]; then
	    write_messages   e 0 3 "Number of detected network connections: ${NET_CONNECTIONS}. Run the netstat -n|awk '/^tcp/{++S[\$NF]}END{for (a in S)print a , "\t",S[a]}' command to check the number."
        NET_NOT_IDLE=1
    else
        NET_NOT_IDLE=0
    fi

    if [[ "${MEM_NOT_IDLE}" -eq 1  ||  "${CPU_NOT_IDLE}" -eq 1  ||  "${DISK_NOT_IDLE}" -eq 1  \
    ||  "${NET_NOT_IDLE}" -eq 1 ]]; then
        exit 1
    else
        exit 0
    fi

}

sys_env_inspectation