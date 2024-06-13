#!/bin/bash
##################################
#Function description: Use ClamAV to scan for viruses.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
##################################

source ~/.bashrc
shopt -s expand_aliases
if [ ! -d "./log" ]; then
    mkdir ./log
fi
CURRENT_PATH=$(pwd)
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
clam_log_file=clam.log_${current_time}


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

clam_scan(){
    scan_path=$1
    file_path="data/test/safety/"
    if [ ! -d "${file_path}" ]; then
        mkdir -p "${file_path}"
    fi
    write_messages  i 0 6 "Perform antivirus scanning test now"
    # update virus database
    freshclam
    # scan directory
    if [ -f ${file_path}${clam_log_file} ];then
        cat /dev/null > ${file_path}${clam_log_file}
    fi
    OLD_IFS="${IFS}"
    IFS=','

    for spath in "${scan_path[@]}"; do
        if ! clamscan -r ${spath}  --log=${file_path}${clam_log_file} 2>&1; then
            write_messages  c 34 6 "An error occurs during the antivirus scanning."
        fi
    done
    IFS="${OLD_IFS}"
}

clamav_install(){
    # Install the virus scan software
    if  hash yum  2>/dev/null && ! hash clamscan 2>/dev/null; then
        if ! yum install -y clamav clamav-update; then
            write_messages  e 0 1 "Failed to install the virus scan software ClamAV. Check the network environment and \
yum source configuration, and install the RPM package of ClamAV."
            exit 1
        fi
    fi
    if hash apt-get 2>/dev/null && ! hash clamscan 2>/dev/null; then
        if ! apt-get install -y clamav; then
            write_messages  e 0 1 "Failed to install the virus scan software ClamAV. Check the network environment and \
apt source configuration, and install the DEB package of ClamAV."
            exit 1
        fi
    fi
}

clamav_install
clam_scan $1