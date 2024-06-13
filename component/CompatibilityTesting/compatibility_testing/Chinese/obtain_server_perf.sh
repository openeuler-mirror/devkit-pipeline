#!/bin/bash
##################################
#CPU、内存、硬盘、网卡和功耗指标采集
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-02-10 修改
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

get_performance() {
    # 进行CPU、内存、硬盘、网卡和功耗指标采集
    # 参数1：采集时间间隔
    # 参数2：采集时长
    # 参数3：采集序列，0：表示兼容测前采集，1：表示性能测试采集，2表示兼容测试后采集
    # 参数4：步骤
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
	times=$((times / 4))
    elif [[ "${seq}" -eq 0 ]]; then
        file_path="data/test/compatiable/"
        file_seq=0
        times=$((times / 4))
    elif [[ "${seq}" -eq 3 ]]; then
        file_path="data/test/compatiable/"
        file_seq=0
        times=$((times / 4))
    else
        file_path="data/test/compatiable/"
        file_seq=1
        times=$((times / 4))
    fi
    desc_array=('兼容性测试前采集' '性能测试采集' '兼容性测试后采集')
    if [[ ! -d "${file_path}" ]]; then
        mkdir -p "${file_path}"
    fi
    file_name="test_perf_cpu_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:调用sar -u ${frequency} ${times}命令采集CPU指标"

    if ! sar -u "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"  "调用sar -u ${frequency} ${times}命令采集CPU指标失败"
        write_messages  s 0 "${step}"  "${desc_array[${seq}]}调用sar -u ${frequency} ${times}命令采集CPU指标失败"
    fi
    file_name="test_perf_mem_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:调用sar -r ${frequency} ${times}命令采集内存指标"
    if ! sar -r "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"  "调用sar -r ${frequency} ${times}命令采集内存指标失败"
        write_messages  s 0 "${step}"  "${desc_array[${seq}]}调用sar -r ${frequency} ${times}命令采集内存指标失败"
    fi
    file_name="test_perf_net_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:调用sar -n DEV ${frequency} ${times}命令采集网卡指标"
    if ! sar -n DEV "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"   "调用sar -n DEV ${frequency} ${times}命令采集网卡指标失败"
        write_messages  s 0 "${step}"   "${desc_array[${seq}]}调用sar -n DEV ${frequency} ${times}命令采集网卡指标失败"
    fi
    file_name="test_perf_disk_${file_seq}.log"
    write_messages  i 0 "${step}" "${desc_array[${seq}]}:调用sar -d -p ${frequency} ${times}命令采集硬盘指标"
    if ! sar -d -p "${frequency}" ${times} >"${file_path}""${file_name}"; then
        write_messages  e 0 "${step}"  "调用sar -d -p ${frequency} ${times}命令采集硬盘指标失败"
        write_messages  s 0  "${step}" "${desc_array[${seq}]}调用sar -d -p ${frequency} ${times}命令采集硬盘指标失败"
    fi
    write_messages  i 0 "${step}" "${desc_array[${seq}]}已完成"
}

get_performance $1 $2 $3 $4
