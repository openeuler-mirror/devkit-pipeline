#!/bin/bash
##################################
#Function description: Use CVE for vulnerabilities scan.
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
cvecheck_log_file=cvecheck.log_${current_time}

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

cve_check(){
    scan_path=$1
    file_path="data/test/safety/"
    CURRENT_PATH=$(pwd)
    LIB_PATH=${CURRENT_PATH}/../lib/
    OLD_IFS="${IFS}"
    IFS=','
    if [[ -e ${LIB_PATH}/cvecheck ]] ; then
        write_messages  i 0 6 "Scan for CVE vulnerabilities now."
        chmod +x ${LIB_PATH}/cvecheck
        ${LIB_PATH}/cvecheck -u
        ${LIB_PATH}/cvecheck -s
        for spath in "${scan_path[@]}"; do
            ${LIB_PATH}/cvecheck -d ${spath} >>./log/"${cvecheck_log_file}"
            cat ${CURRENT_PATH}/cve_tmp/result/cvecheck-result.json >> ${file_path}/cvecheck-result.json 2>> /dev/null;
        done
        write_messages  i 0 6 "Scan completed."
    fi
    IFS="${OLD_IFS}"
}


cve_check $1