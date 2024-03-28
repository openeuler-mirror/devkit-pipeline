#!/bin/bash
##################################
#功能描述: 提供给用户进行兼容性测试、指标日志采集工具
#版本信息: 华为技术有限公司，版权所有（C） 2020-2023
#修改记录：2023-04-08 修改
##################################
# 创建日志目录
clear
if [[ ! -d "./log" ]]; then
    mkdir ./log
fi
CURRENT_PATH=$(pwd)
APP_PID=""
STRESS_CMD=-1
LIB_PATH=${CURRENT_PATH}/../lib/
REMOTE_EXECUTIVE_PATH=/home/compatibility_testing/Chinese/
REMOTE_LIB_PATH=/home/compatibility_testing/lib/
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
app_log_file=app_log.log_${current_time}
nmap_log_file=nmap.log_${current_time}
# 清空日志文件。
log_files=( "${CURRENT_PATH}"/log/"${error_file}" "${CURRENT_PATH}"/log/"${app_log_file}" "${CURRENT_PATH}"/log/"${nmap_log_file}")
for file in "${log_files[@]}"; do
    if [[ -e "${file}" ]]; then
        cat /dev/null >"${file}"
    else
        touch ${file}
    fi
done

# 是否有配置测试工具启动命令。
TEST_TOOL_COMM=0
# 是否有配置应用启动命令。
START_APP_COMM=0
# 是否有配置应用停止命令。
STOP_APP_COMM=0
# 命令执行失败后，手动停止业务应用。
HAND_STOP_APP=0
# 命令执行失败后，手动启动业务应用。
HAND_START_APP=0
# 命令执行失败后，手动启动测试工具。
HAND_START_TEST=0
# 判断是否安装鲲鹏开发套件
HAS_KUNPENG_DEVKIT=0
SYS_LOG_="messages"
DEBUG=0
# 判断是否集群部署
HAS_CLUSTER_ENV=0
KUBERNETES_ENV=0
CLAMAV_SCAN=0
CVE_SCAN=0
HPC_CERTIFICATE=0
BINARY_PACK=0
CURRENT_SYS=""
O_S_VERSION=""
declare -a product_result_array
declare -a snapshot_result_array
declare -a performance_test_pid_array

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
    case ${level_info} in
    i) echo "#${DATE}#info#${step}#${messages}" >> ./log/"${log_file}"
        ;;
    e) echo "#${DATE}#error#${step}#${messages}" >> ./log/"${log_file}"
       echo -e "\033[1;31m${messages}\033[0m"
        ;;
    m) echo "#${DATE}#value#${step}#${messages}" >> ./log/"${log_file}" ;;
    s) echo "#${DATE}#serious#${step}#${messages}" >> ./log/"${error_file}"
       echo -e "\033[1;31m${messages}\033[0m"
        ;;
    c) echo -e "\033[1;${colors}m${messages}\033[0m"
       echo "#${DATE}#info#${step}#${messages}" >> ./log/"${log_file}"
        ;;
    esac
}

notice_users() {
    write_messages  c 34 0 "自动化兼容性测试开始前，请用户先填写配置文件compatibility_testing.conf，填写说明请参考README"
    write_messages  c 34 0 "自动化兼容性测试开始执行，脚本分为10个步骤，运行时间约15分钟，请耐心等待。"
    write_messages  i 0 0  "自动化测试采集工具开始执行。"
}

production_env_waring() {
    write_messages  c 31 0  "测试期间会不断启动和停止待测试应用软件，请勿在生产环境执行兼容性测试工具。确认当前环境不是生产环境，请回复 N ，是生产环境请回复 Y ?"
    read -r INPUT_
    write_messages  c 31 0 "您输入的是 ${INPUT_} "
    if [[ "${INPUT_}" == "Y" ||  "${INPUT_}" == "y" ]]; then
        exit
    fi
}


check_configuration() {
    # 检查用户填写配置项是否为空
    config_file="${CURRENT_PATH}""/compatibility_testing.conf"

    if [[ -f "${config_file}" ]]; then
        application_names=$(sed '/^application_names=/!d;s/application_names=//' "${config_file}")
        start_app_commands=$(sed '/^start_app_commands=/!d;s/start_app_commands=//' "${config_file}")
        stop_app_commands=$(sed '/^stop_app_commands=/!d;s/stop_app_commands=//' "${config_file}")
        start_performance_scripts=$(sed '/^start_performance_scripts=/!d;s/start_performance_scripts=//' "${config_file}")
        cluster_ip_lists=$(sed '/^cluster_ip_lists=/!d;s/cluster_ip_lists=//' "${config_file}")
        kubernetes_env=$(sed '/^kubernetes_env=/!d;s/kubernetes_env=//' "${config_file}")
        cve_scan_path=$(sed '/^cve_scan_path=/!d;s/cve_scan_path=//' "${config_file}")
        clamav_scan_path=$(sed '/^clamav_scan_path=/!d;s/clamav_scan_path=//' "${config_file}")
        hpc_certificate=$(sed '/^hpc_certificate=/!d;s/hpc_certificate=//' "${config_file}")
        binary_file=$(sed '/^binary_file=/!d;s/binary_file=//' "${config_file}")
        if [[ -z "${application_names}" ]]; then
            write_messages  e 31 2 "配置文件中的应用名称为空,请填写正确后重启脚本。"
            exit
        fi
        if [[ -z "${start_app_commands}" ]]; then
            write_messages  e 31 2 "应用启动命令为空。"
            START_APP_COMM=1
        fi
        if [[ -z "${stop_app_commands}" ]]; then
            write_messages  e 31 2 "应用停止命令为空。"
            STOP_APP_COMM=1
        fi
        export STRESS_CMD
        if [[ -z "${start_performance_scripts}" ]]; then
            write_messages  e 31 2  "压力测试工具启动命令为空。"

            STRESS_CMD=0
            TEST_TOOL_COMM=1
        else
            STRESS_CMD=1
        fi
        if [[ -n "${cluster_ip_lists}" ]]; then
            HAS_CLUSTER_ENV=1
        fi
        if [[ -n "${kubernetes_env}" && "${kubernetes_env}" == "Y" ]];then
            KUBERNETES_ENV=1
        fi
        if [[ -n "${cve_scan_path}" ]];then
            CVE_SCAN=1
        fi
        if [[ -n "${clamav_scan_path}" ]];then
            CLAMAV_SCAN=1
        fi
        if [[ -n "${hpc_certificate}" ]];then
            HPC_CERTIFICATE=1
        fi
        if [[ -n "${binary_file}" ]];then
            BINARY_PACK=1
        fi
    else
        write_messages  e 31 2 "配置文件不存在，请检查。"
        exit
    fi
}

create_result_dir() {
    # 创建采集目录
    write_messages  i 0 1 "创建目录"
    result_dirs=("data/hardware/" "data/software/" "data/system/" "data/product/"
    "data/test/performance/" "data/test/compatiable/" "data/test/function/"
    "data/test/power/" "data/test/safety" "data/test/dfx" "data/others")
    for dir in "${result_dirs[@]}"; do
        if [[ ! -d "${dir}" ]]; then
            mkdir -p "${dir}"
        else
            find ./"${dir}" -type f -name "*.log*" -exec rm {} \;
        fi
    done
    # 删除远端服务器的文件
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            ssh root@${ip_addr} "if [ -d ${REMOTE_EXECUTIVE_PATH} ];then cd ${REMOTE_EXECUTIVE_PATH}; rm -rf data log;fi"
        done
    fi
    IFS="${OLD_IFS}"
}

get_service_info() {
    # 获取服务器信息
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    path="data/hardware/"
    if [[ ! -d "${path}" ]]; then
        mkdir -p "${path}"
    fi
    path="data/software/"
    if [[ ! -d "${path}" ]]; then
        mkdir -p "${path}"
    fi
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            scp -r ${CURRENT_PATH}/obtain_service_info.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
            scp ${CURRENT_PATH}/../lib/cvecheck ${ip_addr}:${REMOTE_LIB_PATH} >/dev/null
            ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x obtain_service_info.sh; bash obtain_service_info.sh"
        done
        bash obtain_service_info.sh
    else
        bash obtain_service_info.sh
    fi

    if [[ ${BINARY_PACK} -eq 1 ]]; then
        if [[ -f ${binary_file} ]]; then

            gcc_file="data/software/system_version.log"
            echo -e "\n=${binary_file}=" >>${gcc_file} 2>> /dev/null;
            echo -e "\n=============binary files=============" >>${gcc_file} 2>> /dev/null;
            strings ${binary_file} |grep -E 'GCC|gcc|clang' >>${gcc_file} 2>> /dev/null;
        else
            write_messages e 0 4 "配置文件填写的二进制文件不存在。"
        fi
    fi

    file_name_list=("data/hardware/hardware_info.log" "data/hardware/hardware_pcie.log" "data/hardware/hardware_cpu.log"
    "data/hardware/hardware_disk.log" "data/software/system_version.log")
    smartctl_file_name="data/hardware/hardware_smartctl.log"
    command_desc=("服务器型号" "pci信息" "CPU信息" "硬盘分区" "内核信息")
    length=${#file_name_list[@]}
    for ((i = 0; i < "${length}"; i++)); do
        file_name=${file_name_list[$i]}
        if [[ -f ${file_name} ]]; then
            product_result_array[$i]='True'
        else
            product_result_array[$i]='False'
        fi
    done
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        ip_length=${#ip_list[@]}
        for ((j =0;j<"${ip_length}";j++));do
            ip_addr=${ip_list[$j]}
            for ((i = 0; i < "${length}"; i++)); do
                file_name=${file_name_list[$i]}
                index=$((length * (j + 1) + i))
                scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${file_name} ${file_name}_${ip_addr} >/dev/null
                if [[ $? -eq 0 ]];then
                    product_result_array[$index]='True'
                else
                    product_result_array[$index]='False'
                fi
            done


            scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${smartctl_file_name} ${smartctl_file_name}_${ip_addr} >/dev/null

        done
    fi
    IFS="${OLD_IFS}"
}

get_ps_snapshot() {
    # 获取服务器进程的快照
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    file_path="data/product/"
    if [[ ! -d "${file_path}" ]]; then
        mkdir -p "${file_path}"
    fi
    file_name='product_name.log'
    index=0
    if ! ps aux >${file_path}${file_name}; then
        snapshot_result_array[$index]='False'
        write_messages  e 0 5 "调用ps aux命令获取服务器进程快照失败"
    else
        snapshot_result_array[$index]='True'
    fi
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in "${ip_list[@]}"; do
            index=$(( index + 1 ))
            ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; mkdir -p ${file_path}; ps aux >${file_path}${file_name}"
            scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${file_path}${file_name} ${file_path}${file_name}_${ip_addr} >/dev/null
            if [[ $? -eq 0 ]];then
                snapshot_result_array[$index]='True'
            else
                snapshot_result_array[$index]='False'
            fi
        done
    fi
    IFS="${OLD_IFS}"
}

check_system_message() {
  # 检查系统日志
  # 参数1：开始时间
  # 参数2：结束时间
  start_time=$1
  end_time=$2
  file_path="data/system/"
  file_name="message.log"
  error_file_name="err_messages.log"
  if [[ ! -d "${file_path}" ]]; then
    mkdir -p "${file_path}"
  fi
  system_message_result='True'
  if [[ ${start_time}x == ""x  ||  ${end_time}x == ""x ]]; then
    grep -i -E 'fail|error' /var/log/"${SYS_LOG_}" > "${file_path}""${error_file_name}"
  else
    if ! sed -n /"${start_time}"/,/"${end_time}"/p /var/log/"${SYS_LOG_}" >"${file_path}""${file_name}"; then
      system_message_result='False'
      write_messages   e 0 9 "执行获取服务器日志失败"
      write_messages   s 0 9 "调用sed -n '/${start_time}/,/${end_time}/p' /var/log/${SYS_LOG_}命令失败"
    else
      grep -i -E 'fail|error' "${file_path}""${file_name}" > "${file_path}""${error_file_name}"
    fi
  fi
}

tar_output() {
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    current_time=$(date "+%Y%m%d%H%M%S")
    # 检查文件是否存在
    result_files=("data/hardware/hardware_cpu.log"  "data/hardware/hardware_disk.log"
    "data/hardware/hardware_info.log"  "data/hardware/hardware_pcie.log"
    "data/product/product_name.log" "data/software/system_version.log"
    "data/test/performance/test_perf_cpu_1.log" "data/test/performance/test_perf_disk_1.log"
    "data/test/performance/test_perf_mem_1.log"
    "data/test/performance/test_perf_net_1.log" "data/test/compatiable/test_perf_cpu_0.log"
    "data/test/compatiable/test_perf_cpu_1.log" "data/test/compatiable/test_perf_disk_0.log"
    "data/test/compatiable/test_perf_disk_1.log" "data/test/compatiable/test_perf_mem_0.log"
    "data/test/compatiable/test_perf_mem_1.log" "data/test/compatiable/test_perf_net_0.log"
    "data/test/compatiable/test_perf_net_1.log")
    for data_file in "${result_files[@]}"; do
        if [[ ! -f "${data_file}" ]]; then
            write_messages  e 0 10 "数据目录下的日志文件${data_file}不存在"
        fi
    done
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            for data_file in "${result_files[@]}"; do
                data_file=${data_file}_${ip_addr}
                if [[ ! -f "${data_file}" ]]; then
                    write_messages  e 0 10 "数据目录下的日志文件${data_file}不存在"
                fi
            done
        done
    fi

    record_log_file=( "${error_file}" "${app_log_file}" "${log_file}")
    for file in "${record_log_file[@]}"; do
        if [[ -f ./log/"${file}" ]]; then
            cp ./log/"${file}" ./data/others/
        else
            write_messages  e 0 10 "./log/目录下的日志文件${file}不存在"
        fi
    done
    if [[ -f "${config_file}" ]]; then
        cp "${config_file}" ./data/
    fi
    if ! tar -czf log.tar.gz data; then
        write_messages   e 0 10 "压缩文件出错"
    fi
    write_messages  c  34  10 "采集结束，日志打包完成，压缩包log.tar.gz存放在$CURRENT_PATH。"
    IFS=${OLD_IFS}
}

check_error() {
    # 检查采集期间是否有异常
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    for ((i = 0; i < ${#product_result_array[@]}; i++)); do
        if [[ "${product_result_array[i]}" = 'False' ]]; then
            index=$(($i/5))
            sub_index=$(($i%5))
            if [[ ${index} -eq 0 ]]; then
                desc="本服务器"
            else
                desc=${ip_list[$(($index-1))]}
            fi
            case ${sub_index} in
                0) write_messages   e 0 10 "在${desc}执行获取服务器型号命令失败" ;;
                1) write_messages   e 0 10 "在${desc}执行pcie命令失败" ;;
                2) write_messages   e 0 10 "在${desc}执行lscpu命令失败" ;;
                3) write_messages   e 0 10 "在${desc}执行lsblk命令失败" ;;
                4) write_messages   e 0 10 "在${desc}获取操作系统内核版本失败" ;;
            esac
        fi
    done
    if [[ "${system_message_result}" = 'False' ]]; then
        write_messages   e 0 10 "执行检查系统日志失败，请检查/var/log/${SYS_LOG_}是否有权限查询"
    fi
    for ((i = 0; i < ${#snapshot_result_array[@]}; i++)); do
        if [[ "${snapshot_result_array[i]}" = 'False' ]];then
            if [[ ${i} -eq 0 ]]; then
                write_messages   e 0 10 "在本服务器执行ps查看进程失败"
            else
                index=$(( $i -1))
                write_messages   e 0 10 "在${ip_list[index]}执行ps查看进程失败"
            fi

        fi
    done

    if [[ -f ./log/"${error_file}" ]]; then
        while read -r line; do
            echo "${line}" | awk -F'#' '{if($5~/sar/) print $5}'
        done <./log/"${error_file}"
    fi
    IFS="${OLD_IFS}"
}

# 集群环境检查各IP的是否连通，并获取操作系统版本。
check_connection_and_OS() {
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            ssh root@"${ip_addr}" "mkdir -p  ${REMOTE_EXECUTIVE_PATH};mkdir -p ${REMOTE_LIB_PATH};"
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "${ip_addr} 与当前服务器没有建立免密互信，请配置后重新执行脚本"
                exit
            fi
            scp -r ${CURRENT_PATH}/env_OSVersion.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
            os_version_=$(ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x env_OSVersion.sh; bash env_OSVersion.sh")
            if [[ ${O_S_VERSION}x == ""x ]];then
                O_S_VERSION=${os_version_}
            else
                O_S_VERSION="${O_S_VERSION}"";""${os_version_}"
            fi
        done

    fi
    os_version_=$(chmod +x *.sh; bash env_OSVersion.sh)
    if [[  ${O_S_VERSION}x == ""x ]];then
        O_S_VERSION=${os_version_}
    else
        O_S_VERSION="${O_S_VERSION}"";""${os_version_}"
    fi
    write_messages  i 0 1 "当前的操作系统版本是${O_S_VERSION}。"
    IFS="${OLD_IFS}"
}

# 检查系统环境，安装依赖软件
env_preparation() {
    check_connection_and_OS
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            ssh root@"${ip_addr}" "mkdir -p  ${REMOTE_EXECUTIVE_PATH}"
            scp -r ${CURRENT_PATH}/env_preparation.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} > /dev/null
            ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x env_preparation.sh; bash env_preparation.sh "
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "${ip_addr} 安装依赖软件失败，失败原因请登录该服务器，查看目录${REMOTE_EXECUTIVE_PATH}/log/info.log 的日志"
                exit
            fi
        done
        bash env_preparation.sh
        if [[ $? -ne 0 ]]; then
             write_messages e 0 1 "安装依赖软件失败，失败原因请查看目录${REMOTE_EXECUTIVE_PATH}/log/info.log 的日志"
             exit
        fi

    else
        bash env_preparation.sh
        if [[ $? -ne 0 ]]; then
             write_messages e 0 1 "安装依赖软件失败，失败原因请查看目录${REMOTE_EXECUTIVE_PATH}/log/info.log 的日志"
             exit
        fi

    fi
    IFS="${OLD_IFS}"
}

HCS8_PREFIX="172.36.0.10:58089/rest/v2/virtualMachine/verifyIsHCS?mac="
HCS803_PREFIX="173.64.11.52:58088/rest/v2/virtualMachine/verifyIsHCS?mac="
check_physical_system(){
    # 判断当前系统是否是物理服务器
    OLD_IFS="${IFS}"
    IFS=','
    CURRENT_SYS=""
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            CURRENT_SYS_=$(ssh root@"${ip_addr}" "dmidecode -s system-product-name")
            if [[  ${CURRENT_SYS}x == ""x ]];then
                CURRENT_SYS="${CURRENT_SYS_}"
            else
                CURRENT_SYS="${CURRENT_SYS}"";""${CURRENT_SYS_}"
            fi
        done
    fi
    IFS="${OLD_IFS}"
    CURRENT_SYS_=$(dmidecode -s system-product-name)
    if [[  ${CURRENT_SYS}x == ""x ]];then
        CURRENT_SYS="${CURRENT_SYS_}"
    else
        CURRENT_SYS="${CURRENT_SYS}"";""${CURRENT_SYS_}"
    fi
}

stop_or_start_kunpengdeveloper(){
    step=$1
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            ssh root@"${ip_addr}" "mkdir -p  ${REMOTE_EXECUTIVE_PATH}"
            scp -r ${CURRENT_PATH}/env_kunpengdeveloper.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
            ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x env_kunpengdeveloper.sh; bash env_kunpengdeveloper.sh ${step}"
        done
        bash env_kunpengdeveloper.sh ${step}

    else
        bash env_kunpengdeveloper.sh ${step}

    fi
    IFS="${OLD_IFS}"
}

sys_env_inspectation() {
    # 从CPU、内存、硬盘和网卡四个角度检查利用率是否过高，如果过高则提升当前环境非空闲
    write_messages  i 0 3 "环境自检开始"
    OLD_IFS="${IFS}"
    IFS=','
    ENV_NOT_IDLE=0
    exam_time=0
    read -r -a ip_list <<< "$cluster_ip_lists"
    while [[ "${exam_time}" -lt 5 ]]; do
        ENV_NOT_IDLE=0
        if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
            for ip_addr in  "${ip_list[@]}"; do
                ssh root@"${ip_addr}" "mkdir -p  ${REMOTE_EXECUTIVE_PATH}"
                scp -r ${CURRENT_PATH}/env_inspectation.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
                ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x env_inspectation.sh; bash env_inspectation.sh"
                if [[ $? -ne 0 ]]; then
                    write_messages e 0 1 "服务器为${ip_addr}的环境非空闲，请停止运行的业务软件，详细信息请登录该服务器，查看目录${REMOTE_EXECUTIVE_PATH}/log/info.log 的日志"
                    ENV_NOT_IDLE=1
                fi
            done
            bash env_inspectation.sh
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "当前服务器的环境非空闲，请停止运行的业务软件，详细信息查看目录${REMOTE_EXECUTIVE_PATH}/log/info.log 的日志"
                ENV_NOT_IDLE=1
            fi
        else
            bash env_inspectation.sh
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "当前服务器的环境非空闲，请停止运行的业务软件，详细信息查看目录${REMOTE_EXECUTIVE_PATH}/log/info.log 的日志"
                ENV_NOT_IDLE=1
            fi
        fi
        if [[ "${ENV_NOT_IDLE}" -eq 1 ]] ; then
            exam_time=$((exam_time + 1))
        else
            write_messages  i 0 3 "环境自检结束"
            break
        fi
    done
    if [[ "${exam_time}" -eq 5 ]]; then
        write_messages   e 0 3 "环境自检没有通过，请用户确保停止所有业务应用及其依赖软件后重新执行脚本。如果需要跳过环境自检，请回复 Y。跳过环境自检会影响测试采集，导致测试结果不准确。如需停止脚本，请回复N。"
        read -r INPUT_
        write_messages  e 0 3 "您输入的是 ${INPUT_} "
        if [[ "${INPUT_}" == "Y" ||  "${INPUT_}" == "y" ]]; then
           write_messages  i 0 3 "环境自检跳过"
        else
           exit
        fi
    fi
    IFS="${OLD_IFS}"

}

port_scan() {
    # 安全扫描，使用nmap进行端口扫描
    OLD_IFS="${IFS}"
    IFS=','
    scan_pid=""
    scan_process=""
    read -r -a ip_list <<< "$cluster_ip_lists"
    file_path="data/test/safety/"
    if [[ ! -d "${file_path}" ]]; then
        mkdir -p "${file_path}"
    fi
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            {
            scp ${CURRENT_PATH}/security_port_scan.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
            ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x security_port_scan.sh; bash security_port_scan.sh"
            ssh root@"${ip_addr}" "cd ${REMOTE_EXECUTIVE_PATH}${file_path}; for file in \$(find . -type f -name '*.nmap' -exec basename {} \; );do mv \${file} ${ip_addr}_\${file};done"
            scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${file_path}${ip_addr}*.nmap ${file_path} >/dev/null
            } &
            scan_pid=$!
            if [[ -n ${scan_process} ]];then
                scan_process="${scan_process},${scan_pid}"
            else
                scan_process=${scan_pid}
            fi
        done
        for pid in ${scan_process};do
            wait ${pid}
        done
    fi
    bash security_port_scan.sh
    write_messages  i 0 6 "端口安全测试结束"
    IFS="${OLD_IFS}"
}

validated_clamav_scan(){
    if [[ "${CLAMAV_SCAN}" -eq 1 ]]; then
        write_messages  i 0 6 "防病毒扫描开始"
        scan_path_parser 1
        write_messages  i 0 6 "防病毒扫描结束"
    fi
}


validated_cvecheck(){
    if [[ "${CVE_SCAN}" -eq 1 ]]; then
        # 使用cvecheck进行漏洞扫描
        write_messages  i 0 6 "漏洞扫描开始"
        scan_path_parser 2
        write_messages  i 0 6 "漏洞扫描结束"
    fi
}

scan_path_parser(){
    step=$1
    OLD_IFS="${IFS}"
    IFS=','
    scan_pid=""
    scan_process=""
    scan_ip_desc=":"
    current_server_path=""
    file_path="data/test/safety/"
    declare -A multiple_path_array
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${step}" -eq 1 ]]; then
        read -r -a scan_path_list <<< "$clamav_scan_path"
        file_name=clam.log_${current_time}
    else
        read -r -a scan_path_list <<< "$cve_scan_path"
        file_name=cvecheck-result.json
    fi
    if [[ ! -d "${file_path}" ]]; then
        mkdir -p "${file_path}"
    fi
    for scan_path  in "${scan_path_list[@]}"; do
        if [[ ${scan_path} == *${scan_ip_desc}* ]]; then
            ip=${scan_path%:*}
            path=${scan_path#*:}
            if [[ "${ip_list[@]}"=~"${ip}" ]]; then
                if [[  -n ${multiple_path_array[$ip]} ]];then
                    multiple_path_array[$ip]="${multiple_path_array[$ip]},${path}"
                else
                    multiple_path_array[$ip]=${path}
                fi
            fi
        else
            if [[ -n ${current_server_path} ]];then
                current_server_path="${current_server_path},${scan_path}"
            else
                current_server_path=${scan_path}
            fi
        fi
    done

    for ip_addr in  "${ip_list[@]}"; do
        if [[ -n ${multiple_path_array[$ip_addr]} ]]; then
            if [[ "${step}" -eq 1 ]]; then
                {
                scp ${CURRENT_PATH}/security_clam_scan.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
                ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x security_clam_scan.sh; bash security_clam_scan.sh ${multiple_path_array[$ip_addr]}"
                scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${file_path}${file_name} ${file_path}${file_name}_${ip_addr} >/dev/null
                } &
            else
                {
                scp ${CURRENT_PATH}/security_cvecheck.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
                ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x security_cvecheck.sh; bash security_cvecheck.sh ${multiple_path_array[$ip_addr]}"
                scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${file_path}${file_name} ${file_path}${file_name}_${ip_addr} >/dev/null
                } &
            fi
            scan_pid=$!
            if [[ -n ${scan_process} ]];then
                scan_process="${scan_process},${scan_pid}"
            else
                scan_process=${scan_pid}
            fi
        fi
    done
    for pid in ${scan_process};do
        wait ${pid}
    done
    IFS="${OLD_IFS}"
    if [[ -n ${current_server_path} ]];then
        if [[ "${step}" -eq 1 ]]; then
            bash security_clam_scan.sh ${current_server_path}
        else
            bash security_cvecheck.sh ${current_server_path}
        fi
    fi
    unset scan_pid
    unset scan_process
    unset scan_ip_desc
    unset current_server_path
    unset multiple_path_array
}


check_process(){
    # 检查进程是否存在
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    process_name=$1
    process_name=$(echo ${process_name} | awk '{gsub(/^\s+|\s+$/, "");print}')
    ps_results=""
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            if [[ ${KUBERNETES_ENV} -eq 1 ]];then
                ps_result="$(ssh root@${ip_addr} "kubectl get all --all-namespaces |grep -i ${process_name} 2>/dev/null")"
            else
                ps_result="$(ssh root@${ip_addr} "pgrep -lf ${process_name}")"
            fi
            if [[ "${ps_result}" != '' ]]; then
                ps_results="${ps_results}"";""${ps_result}"
            fi
        done
    fi
    if [[ ${KUBERNETES_ENV} -eq 1 ]];then
        ps_result=$(kubectl  get all --all-namespaces |grep -i ${process_name} 2>/dev/null)
    else
        ps_result=$(pgrep -lf "${process_name}")
    fi
    if [[ "${ps_result}" != '' ]]; then
        ps_results="${ps_results}"";""${ps_result}"
    fi
    echo "${ps_results}"
    IFS="${OLD_IFS}"
}

check_process_exits_stop() {
    # 检查业务应用是否存在，如果存在停止进程。
    step=$1
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a app_list <<< "$application_names"
    read -r -a stop_comm_list <<< "$stop_app_commands"
    length=${#app_list[@]}
    sleep_time=10
    CURRENT_PATH=$(pwd)
    STOP_APP_FLAG=0
    for ((i = 0; i < "${length}"; i++)); do
        process=${app_list[$i]}
        process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
        ps_result="$(check_process ${process})"
        if [[ "${ps_result}" != "" ]]; then
            write_messages  i 34 "${step}" "进程${process}存在"
            STOP_APP_FLAG=1
        else
            if [[ ${step} -ne  4 ]]; then
                write_messages  i 34 "${step}" "进程${process}不存在"
            fi
        fi
    done
    if [[ "${step}" -eq 9   &&  "${STOP_APP_FLAG}" -eq 0 ]] ; then
        for stop_comm in  "${stop_comm_list[@]}"; do
            eval "${stop_comm}" >>  "${CURRENT_PATH}"/log/"${app_log_file}"  2>&1
            if [[ "$?" -ne 0 ]]; then
                cd "${CURRENT_PATH}"||exit
                write_messages  e 0 "${step}" "执行${stop_comm}出错"
            else
            cd "${CURRENT_PATH}"||exit
            fi
        done
    fi
    if [[ "${STOP_APP_COMM}" -eq 0   &&  "${STOP_APP_FLAG}" -eq 1 ]] ; then
        for stop_comm in  "${stop_comm_list[@]}"; do
            eval "${stop_comm}" >>  "${CURRENT_PATH}"/log/"${app_log_file}"  2>&1
            if [[ "$?" -ne 0 ]]; then
                cd "${CURRENT_PATH}"||exit
                write_messages  e 0 "${step}" "执行${stop_comm}出错"
            else
                cd "${CURRENT_PATH}"||exit
            fi
        done
        for ((i = 0; i < "${length}"; i++)); do
            process=${app_list[$i]}
            process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
            check_times=5
            while [[ ${check_times} -gt 0 ]];do
                sleep "${sleep_time}"
                ps_result="$(check_process ${process})"
                if [[ "${ps_result}" != "" ]]; then
                    write_messages  e 0 "${step}" "执行${stop_comm}后，等待${sleep_time}秒，${process}存在"
                    kill_process ${process}
                    check_times=$((check_times -1))
                else
                    if [[ ${step} -ne  4 ]]; then
                        write_messages  i 34 "${step}" "进程${process}不存在"
                    fi
                    break
                fi
            done
            ps_result="$(check_process ${process})"
            if [[ ${check_times} -le 0 ]] ; then
                write_messages  e 0 "${start_step}" "停止业务应用${process}失败，请用户检查停止脚本。"
                HAND_STOP_APP=1
            fi
        done
    fi
    if [[ "${STOP_APP_COMM}" -eq 1  ]] || [[ "${HAND_STOP_APP}" -eq 1 ]]; then
        for ((i = 0; i < "${length}"; i++)); do
            process=${app_list[$i]}
            process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
            ps_result="$(check_process ${process})"
            check_times=5
            export APP_PID
            echo $APP_PID
            if ! [ -z "$APP_PID" ]; then
              kill -9 $APP_PID
              APP_PID=""
            fi
            if [[ ${check_times} -le 0 ]]; then
                write_messages  e 0 "${step}" "检查到应用程序还在启动，且用户未能停止应用，请用户停止应用后再执行脚本"
                exit
            else
                write_messages  i 0 "${step}" "进程${process}不存在"
            fi
        done
    fi
    IFS="${OLD_IFS}"
}

kill_process() {
    # 强制杀死进程
    OLD_IFS="${IFS}"
    IFS=','
    process_name=$1
    kill_results=""
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            kill_result="$(ssh root@${ip_addr} "pgrep -f "${process}" | xargs kill -9")"
            if [[ "${ps_result}" != '' ]]; then
                kill_results="${kill_result}"";""${kill_result}"
            fi
        done
    fi
    kill_result="$(pgrep -f "${process}" | xargs kill -9 >>./log/"${log_file}"  2>&1)"
    if [[ "${kill_result}" != '' ]]; then
        kill_results="${kill_result}"";""${kill_result}"
    fi
    IFS="${OLD_IFS}"
}

counting_time(){
    ds=$1
    (
        tput sc
        for ((dsec = "${ds}"; dsec > 0; dsec--)); do
            min=$((dsec / 60))
            se=$((dsec % 60))
            tput rc
            tput ed
            echo -ne "\r 采集剩余时间：${min}:${se}\r"
            sleep 1
        done
    ) &
}



get_performance() {
    # 进行CPU、内存、硬盘、网卡和功耗指标采集
    # 参数1：采集时间间隔
    # 参数2：采集时长
    # 参数3：采集序列，0：表示兼容测前采集，1：表示性能测试采集，2表示兼容测试后采集
    # 参数4：步骤
    OLD_IFS="${IFS}"
    IFS=','
    frequency=$1
    seq=$3
    during_time=$2
    step=$4
    times=$((during_time * 60 / frequency))
    seconds=$((during_time * 60))
    if [[ "${seq}" -eq 1 ]]; then
        file_path="data/test/performance/"
        file_seq=1
        d_seconds=$((during_time * 60 *4))
    elif [[ "${seq}" -eq 0 ]]; then
        file_path="data/test/compatiable/"
        file_seq=0
        d_seconds=$((during_time * 60 *5))
    else
        file_path="data/test/compatiable/"
        file_seq=1
        d_seconds=$((during_time * 60 *5))
    fi
    power_path="data/test/power/"
    if [[ ! -d "${power_path}" ]]; then
        mkdir -p "${power_path}"
    fi
    desc_array=('兼容性测试前采集' '性能测试采集' '兼容性测试后采集')
    if [[ ! -d "${file_path}" ]]; then
        mkdir -p "${file_path}"
    fi
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            {
                ssh root@"${ip_addr}" "mkdir -p  ${REMOTE_EXECUTIVE_PATH}; pgrep -f obtain_server_perf.sh | xargs kill -9 "
                scp -r ${CURRENT_PATH}/obtain_server_perf.sh  ${ip_addr}:${REMOTE_EXECUTIVE_PATH} >/dev/null
                ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x obtain_server_perf.sh; bash obtain_server_perf.sh ${frequency} ${during_time} ${seq} ${step}"
            }&
        done
    fi
    counting_time d_seconds
    bash obtain_server_perf.sh ${frequency} ${during_time} ${seq} ${step}
    file_list=("test_perf_cpu_${file_seq}.log" "test_perf_mem_${file_seq}.log" "test_perf_net_${file_seq}.log" "test_perf_disk_${file_seq}.log")
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        ip_length=${#ip_list[@]}
        for ((j =0;j<"${ip_length}";j++));do
            ip_addr=${ip_list[$j]}
            for ((i = 0; i < 4; i++)); do
                file_name=${file_list[$i]}
                scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${file_path}${file_name} ${file_path}${file_name}_${ip_addr} >/dev/null
            done
            if [[ "${seq}" -ne 1 ]]; then
                file_name="test_power_${file_seq}.log"
                scp ${ip_addr}:${REMOTE_EXECUTIVE_PATH}${power_path}${file_name} ${power_path}${file_name}_${ip_addr} >/dev/null
            fi
        done
    fi
    write_messages  i 0 "${step}" "${desc_array[${seq}]}已完成"
    IFS="${OLD_IFS}"
}

start_app(){
    # 启动业务应用进程
    start_step=$1
    export APP_PID
    if [[ ${start_step} -eq 5 ]];then
        check_process_exits_stop "${start_step}"
    fi
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a app_list <<< "$application_names"
    read -r -a start_comm_list <<< "$start_app_commands"
    length=${#app_list[@]}
    START_APP_FLAG=0
    SLEEP_TIME=10
    if [[ ${START_APP_COMM} -eq 0 ]];then
        for start_comm in  "${start_comm_list[@]}"; do
            eval "${start_comm}" >> "${CURRENT_PATH}"/log/"${app_log_file}" 2>&1
            APP_PID=$!
            if [ $? -ne 0 ]; then
                cd "${CURRENT_PATH}"||exit
                write_messages  e 0 "${start_step}" "执行${start_comm}报错，请手动执行命令启动业务应用"
                START_APP_FLAG=1
            fi
	        cd "${CURRENT_PATH}"||exit
	    done
	    for ((i = 0; i < "${length}"; i++)); do
	        process=${app_list[$i]}
	        process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
            check_times=5
            while [[ ${check_times} -gt 0 ]];do
                sleep "${SLEEP_TIME}"
                ps_result="$(check_process ${process})"
                if [[ "${ps_result}" != "" ]]; then
                      write_messages  i 0 "${start_step}" "业务应用${process}启动完成。"
                      break
                else
                    check_times=$((check_times -1))
                fi
            done
            if [[ ${check_times} -le 0 ]]; then
                write_messages  e 0 "${start_step}" "启动业务应用${process}失败，请用户检查启动脚本。"
                START_APP_FLAG=1

            fi
        done
    fi
    if [[ "${start_step}" -eq 5 ]];then
        if [[ ${START_APP_FLAG} -eq 1 ||  ${START_APP_COMM} -eq 1 ]]; then
            for ((i = 0; i < "${length}"; i++)); do
                process=${app_list[$i]}
                process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
                ps_result="$(check_process ${process})"
                if [[ "${ps_result}" != "" ]]; then
                    write_messages  c 34 "${start_step}" "业务应用${process}启动完成。"
                fi
                if [[ "${ps_result}" == "" ]]; then
                    write_messages  e 0 "应用启动失败请检查应用启动命令"
                    exit 1
                fi
            done
        fi
    fi
    IFS="${OLD_IFS}"
}

start_performance_test(){
    # 调用压力测试命令进行压力测试。
    PERF_TEST_FLAG=0
    export STRESS_CMD
    if [[ $STRESS_CMD -eq 1 ]]; then
        write_messages  i 0 7 "启动压力测试工具"
        if [[ ${TEST_TOOL_COMM} -eq 0 ]];then
            OLD_IFS="${IFS}"
            IFS=','
            read -r -a start_performance_scripts <<< "$start_performance_scripts"
            length=${#start_performance_scripts[@]}
            for ((i = 0; i < "${length}"; i++)); do
                start_script=${start_performance_scripts[$i]}
                pattern='&$'
                if  [[ $start_script =~ $pattern  ]];then
                    eval "${start_script}" >>  ./log/"${app_log_file}"  2>&1
                    PID=$!
                cd "${CURRENT_PATH}"||exit
                else
                    eval "(${start_script})&" >>  ./log/"${app_log_file}"  2>&1
                    PID=$!
                cd "${CURRENT_PATH}"||exit
                fi

                if ! ps -fp "${PID}"> /dev/null; then
                    write_messages  e 0 7 "调用命令${start_script}启动测试工具失败"
                    HAND_START_TEST=1
                    PERF_TEST_FLAG=0
                else
                    performance_test_pid_array[$i]="${PID}"
                    PERF_TEST_FLAG=1
                fi
            done
            IFS="${OLD_IFS}"
        fi
        if [[ ${TEST_TOOL_COMM} -eq 1  ||  ${PERF_TEST_FLAG} -eq 0 ]];then
            write_messages  c 31 7  "启动压力测试工具失败，请确认启动命令"
            exit 1
        fi
    else
        write_messages  i 0 7 "用户未提供启动压力测试工具命令"
    fi
}

reliablity_test(){
    # 可靠性测试
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a app_list <<< "$application_names"
    read -r -a start_comm_list <<< "$start_app_commands"
    SLEEP_TIME=20
    RELIABLE_TEST_FLAG=0

    for pid in "${performance_test_pid_array[@]}"
    do
      # 当前pid的所有子进程
        sub_pids=$(pgrep -P ${pid})
        for subpid in ${sub_pids};
        do
            kill -9  "${subpid}" >>./log/"${log_file}"  2>&1
        done
        disown "${pid}"
        kill -9  "${pid}" >>./log/"${log_file}"  2>&1
        if [[ $? -ne 0 ]] ; then
            write_messages   e 0 8 "压力测试工具停止失败"
            exit 1
        fi
        sleep "${SLEEP_TIME}"
    done

    if [[ ${KUBERNETES_ENV} -eq 0  &&  ${HAND_START_APP} -eq 0 && ${START_APP_COMM} -eq 0 ]];then
        length=${#app_list[@]}
        for ((i = 0; i < "${length}"; i++));
        do
            process=${app_list[$i]}
            process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
            ps_result="$(check_process ${process})"
            if [[ "${ps_result}" != "" ]]; then
                write_messages  i 0 8 "可靠性测试前检查，业务应用进程${process}存在"
                if ! kill_process ${process} >>./log/"${log_file}"  2>&1 ; then
                    write_messages  e 0 8 "可靠性测试，执行强制杀死进程${process}报错，可靠性测试失败。"
                    RELIABLE_TEST_FLAG=1
                else
                    write_messages  i 0 8 "可靠性测试，执行强制杀死进程${process}"
                fi
            else
                write_messages  e 31 8 "可靠性测试前，业务应用${process}已停止，可靠性测试失败。"
                RELIABLE_TEST_FLAG=1
            fi
        done
        sleep "${SLEEP_TIME}"
        if [[ ${RELIABLE_TEST_FLAG} -eq 0 ]]; then
            start_app 8
            for ((i = 0; i < "${length}"; i++));
            do
                process=${app_list[$i]}
                process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
                ps_result="$(check_process ${process})"
                if [[ "${ps_result}" != "" ]]; then
                    write_messages  i 0 8 "可靠性测试，业务应用${process}启动完成。可靠性测试成功"
                else
                    write_messages  e 31 8 "可靠性测试，业务应用${process}启动失败,可靠性测试失败。"
                fi
            done
        fi
    elif [[  ${KUBERNETES_ENV} -eq 1 ]]; then
        write_messages  c 31 8 "业务应用是Kubernetes集群，不支持该用例测试。"
    else
        write_messages  c 31 8 "业务应用是用户手动启动，不支持该用例测试。"
    fi
    IFS="${OLD_IFS}"
}


notice_users

if [[ "$(id -u)" -ne 0 ]]; then
  write_messages  c 31 0 "请使用root用户权限执行脚本，否则没有权限检查系统日志。"
fi

# 1、检查配置文件是否正确
write_messages  c  34 1 "第 1 步：配置文件检查, 开始"
check_configuration
write_messages  c  34 1 "第 1 步：配置文件检查, 完成"
# 初始化，创建采集目录树
write_messages  c 34  1 "第 2 步：软件依赖检查，开始"
create_result_dir

# 0 环境准备，判断用户环境
env_preparation
check_physical_system
# 判断鲲鹏开发套件是否有启动，启动则调用命令停止
stop_or_start_kunpengdeveloper 1

message_start_time="$(tail -1 /var/log/${SYS_LOG_}| awk -F' ' '{for(i=1;i<=3;i++) printf $i OFS}')"
write_messages  c  34 1 "第 2 步：软件依赖检查，完成"

# 2、环境自检
write_messages  c  34 3 "第 3 步：测试环境自检, 开始"
sys_env_inspectation
write_messages  c  34 3 "第 3 步：测试环境自检, 完成"

write_messages  c  34 4 "第 4 步：应用启动前CPU、内存、硬盘、网卡和功耗系统资源采集"
# 3、系统信息采集
get_service_info

# 4、idle_0采集
# 4.1、检查应用进程，若存在则停止
check_process_exits_stop 4
# 4.2、idle_0采集
get_performance 5 1 0 4
write_messages  c  34 4 "第 4 步：应用启动前采集结束"
sleep 10
# 5、启动业务
write_messages  c  34 5 "第 5 步：启动业务应用，可通过./log目录info.log查看进度，如果长时间未进行下一步，请检查配置文件填写的启动命令执行后是否会返回。"
start_app 5
get_ps_snapshot
write_messages  c  34 5 "第 5 步：启动业务应用完成"

# 6、安全检查
write_messages  c  34 6 "第 6 步：安全测试，进行应用端口扫描，可通过./log目录的nmap.log查看进度"
port_scan
validated_clamav_scan
validated_cvecheck
write_messages  c  34 6 "第 6 步：安全测试结束"

# 7.1、进行压力测试
write_messages  c 34 7 "第 7 步：进行业务压力下CPU、内存、硬盘和网卡系统资源采集"
if [[ ${HPC_CERTIFICATE} -eq 0 && ${STRESS_CMD} -eq 1 ]]; then
    start_performance_test
    # 7.2、压力测试采集
    sleep 2
    get_performance 5 3 1 7
    sleep 20
fi


write_messages  c  34 7 "第 7 步：压力测试采集结束."


# 8、可靠性测试
write_messages  c  34 8 "第 8 步：进行可靠性测试，强制KILL应用后正常启动测试"
if [[ ${HPC_CERTIFICATE} -eq 0 ]]; then
    reliablity_test
fi
write_messages  c  34 8 "第 8 步：可靠性测试结束."

# 9、idle_1测试
write_messages  c  34 9 "第 9 步：应用停止后CPU、内存、硬盘、网卡和功耗系统资源采集"
check_process_exits_stop 9
get_performance 5 1 2 9
sleep 10
write_messages  c  34 9 "第 9 步：应用停止后资源采集结束"

write_messages  c  34 10 "第 10 步：测试采集数据打包"
if [[ ${HPC_CERTIFICATE} -eq 1 ]]; then
    source ./hpc_linpack_test.sh
    bash hpc_memory_bandwidth.sh
fi
stop_or_start_kunpengdeveloper 10
message_end_time="$(tail -1 /var/log/${SYS_LOG_}| awk -F' ' '{for(i=1;i<=3;i++) printf $i OFS}')"
check_system_message "${message_start_time}" "${message_end_time}"
# 检查以上采集是否有出错
check_error
tar_output
exit
