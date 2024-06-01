#!/bin/bash
##################################
#功能描述: 使用CVEcheck进行漏洞扫描测试
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-02-10 修改
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

cve_check(){
    scan_path=$1
    file_path="data/test/safety/"
    CURRENT_PATH=$(pwd)
    LIB_PATH=${CURRENT_PATH}/../lib/
    OLD_IFS="${IFS}"
    IFS=','
    if [[ -e ${LIB_PATH}/cvecheck ]] ; then
        write_messages  i 0 6 "现在CVE_CHECK漏洞扫描测试"
        chmod +x ${LIB_PATH}/cvecheck
        ${LIB_PATH}/cvecheck -u
        ${LIB_PATH}/cvecheck -s
        for spath in "${scan_path[@]}"; do
            ${LIB_PATH}/cvecheck -d ${spath} >>./log/"${cvecheck_log_file}"
            cat ${CURRENT_PATH}/cve_tmp/result/cvecheck-result.json >> ${file_path}/cvecheck-result.json 2>> /dev/null;
        done
        write_messages  i 0 6 "漏洞扫描结束"
    fi
    IFS="${OLD_IFS}"
}


cve_check $1