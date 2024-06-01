#!/bin/bash
##################################
#功能描述: 从CPU、内存、硬盘和网卡四个角度检查利用率是否过高，如果过高则提升当前环境非空闲
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-02-10 修改
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
# 空闲标记
CPU_NOT_IDLE=0
MEM_NOT_IDLE=0
NET_NOT_IDLE=0
DISK_NOT_IDLE=0

# 最大检查次数
MAX_CPU_IDLE=10.00
MAX_MEM_IDLE=5.00
MAX_DISK_IDLE=5.00
MAX_NET_CONNS=100

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

sys_env_inspectation() {
    # 环境自检，检查CPU、内存、硬盘和网卡是否利用率是否过高。
    # 脚本的PID
    SCRIPTS_PID=$(ps -ef | grep -v grep | grep "$0" | awk '{print $2}')

    DISK_NAME=$(iostat -d -x |sed -n '4,$p'|wc -l)
    IOS_LINE=$(( ( DISK_NAME + 1 )*4 +2 ))

    CPU_IDLE=$(top -n 1 -b | head -10 | tail -3 | awk '{if ($12 !~/top/ && $1!~/"${SCRIPTS_PID}"/) print $9}' | head -1)
    if [[ -z "${CPU_IDLE}" ]]; then
        write_messages   e 0 3 "环境自检，检查CPU利用率出错,调用top命令出错"
    elif [[ "$(echo "${CPU_IDLE}>${MAX_CPU_IDLE}" | bc)" -eq 1 ]]; then
        write_messages   e 0 3 "环境自检，检测到应用CPU利用率为：${CPU_IDLE}大于阈值,请使用top命令检查"
        CPU_NOT_IDLE=1
    else
        CPU_NOT_IDLE=0
    fi
    MEM_IDLE=$(top -n 1 -b | head -10 | tail -3 | awk '{if ($12 !~/top/ && $1!~/"${SCRIPTS_PID}"/) print $10}' | head -1)
    write_messages  i 0 3 "环境自检，检测到应用内存利用率为：${MEM_IDLE}"
    if [[ -z "${MEM_IDLE}" ]]; then
        write_messages   e 0 3 "环境自检，检查内存利用率出错,调用top命令出错"
    elif [[ "$(echo "${MEM_IDLE}>${MAX_MEM_IDLE}" | bc)" -eq 1 ]]; then
        write_messages   e 0 3 "环境自检，检测到应用内存利用率为：${MEM_IDLE}大于阈值,请使用top命令检查"
        MEM_NOT_IDLE=1
    else
        MEM_NOT_IDLE=0
    fi
    # 调用iostat检查硬盘的%util 设备的带宽利用率
    DISK_IDLE=$(iostat -d -x 1 5 |sed -n ''${IOS_LINE}',$p' |grep -v Device|awk '{print $NF}'|sort -nr|head -1)
    write_messages  i 0 3 "环境自检，检测到硬盘的带宽利用率为：${DISK_IDLE}"
    if [[ -z "${DISK_IDLE}" ]]; then
        write_messages   e 0 3 "环境自检，检查硬盘利用率出错,调用iostat命令出错。"
    elif [[ "$(echo "${DISK_IDLE}>${MAX_DISK_IDLE}" | bc)" -eq 1 ]]; then
        write_messages   e 0 3 "环境自检，检测到硬盘的带宽利用率为：${DISK_IDLE}大于阈值，请使用iostat -d -x命令检查"
        DISK_NOT_IDLE=1
    else
        DISK_NOT_IDLE=0
    fi
    # 调用netstat检查网络连接情况
    NET_CONNECTIONS=$(netstat -n | awk '/^tcp/ {++S[$NF]}END{for(a in S) print S[a]}' | sort -nr | head -1)
    write_messages  i 0 3 "检测到网络连接数：${NET_CONNECTIONS}"
    if [[ -z "${NET_CONNECTIONS}" ]]; then
        write_messages   e 0 3 "环境自检，检查网络连接数出错,调用nestat命令出错。"
    elif [[ "${NET_CONNECTIONS}" -ge "${MAX_NET_CONNS}" ]]; then
	    write_messages   e 0 3 "检测到网络连接数：${NET_CONNECTIONS}，请使用 netstat -n|awk '/^tcp/{++S[\$NF]}END{for (a in S)print a , "\t",S[a]}'检查"
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