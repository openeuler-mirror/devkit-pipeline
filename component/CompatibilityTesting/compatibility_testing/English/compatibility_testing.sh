#!/bin/bash
##################################
#Function description: An indicator collection tool used for compatibility and performance tests.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2023
#Change history: Modified on 2023-04-08
##################################
# Create a log directory.
clear
if [[ ! -d "./log" ]]; then
    mkdir ./log
fi
CURRENT_PATH=$(pwd)
LIB_PATH=${CURRENT_PATH}/../lib/
REMOTE_EXECUTIVE_PATH=/home/compatibility_testing/Chinese/
REMOTE_LIB_PATH=/home/compatibility_testing/lib/
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}
app_log_file=app_log.log_${current_time}
nmap_log_file=nmap.log_${current_time}
# Clear all log files.
log_files=( "${CURRENT_PATH}"/log/"${error_file}" "${CURRENT_PATH}"/log/"${app_log_file}" "${CURRENT_PATH}"/log/"${nmap_log_file}")
for file in "${log_files[@]}"; do
    if [[ -e "${file}" ]]; then
        cat /dev/null >"${file}"
    else
        touch ${file}
    fi
done

# Check whether the command for starting the test tool is available.
TEST_TOOL_COMM=0
# Check whether the command for starting the application is available.
START_APP_COMM=0
# Check whether the command for stopping the application is available.
STOP_APP_COMM=0
# Manually stop the application after the command fails to be executed.
HAND_STOP_APP=0
# Manually start the application after the command fails to be executed.
HAND_START_APP=0
# Manually start the test tool after the command fails to be executed.
HAND_START_TEST=0
# Check whether Kunpeng DevKit has been installed
HAS_KUNPENG_DEVKIT=0
SYS_LOG_="messages"
DEBUG=0
# Check whether the application is deployed as a cluster.
HAS_CLUSTER_ENV=0
KUBERNETES_ENV=0
CLAMAV_SCAN=0
CVE_SCAN=0
CURRENT_SYS=""
O_S_VERSION=""
declare -a product_result_array
declare -a snapshot_result_array
declare -a performance_test_pid_array

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
    write_messages  c 34 0 "Before starting automatic collection, you must configure compatibility_testing.conf. Please refer to the README for instructions."
    write_messages  c 34 0 "The automatic test tool starts to execute the script. The script consists of 10 steps and takes about 50 minutes."
    write_messages  i 0 0  "The automatic test tool starts to run."
}

production_env_waring() {
    write_messages  c 31 0  "Do not run the compatibility test tool in the production environment because the application to be tested will be repeatedly started\
 and stopped during the test. Confirm whether the current environment is a production environment. If yes, enter Y. If no, enter N."
    read -r INPUT_
    write_messages  c 31 0 "You've entered ${INPUT_} "
    if [[ "${INPUT_}" == "Y" ||  "${INPUT_}" == "y" ]]; then
        exit
    fi
}


check_configuration() {
    # Check whether any mandatory configuration item is empty.
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
        if [[ -z "${application_names}" ]]; then
            write_messages  e 31 2 "The application name in the configuration file is empty. Enter a correct name and restart the script."
            exit
        fi
        if [[ -z "${start_app_commands}" ]]; then
            write_messages  e 31 2 "The command for starting the application is empty."
            START_APP_COMM=1
        fi
        if [[ -z "${stop_app_commands}" ]]; then
            write_messages  e 31 2 "The command for stopping the application is empty."
            STOP_APP_COMM=1
        fi
        if [[ -z "${start_performance_scripts}" ]]; then
            write_messages  e 31 2  "The command for starting the pressure test tool is empty."
            TEST_TOOL_COMM=1
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
    else
        write_messages  e 31 2 "The configuration file does not exist."
        exit
    fi
}

create_result_dir() {
    # Create Directory
    write_messages  i 0 1 "Create Directory"
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
    # Delete files from the remote server.
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
    # Obtain Server Info
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
            scp -r ${CURRENT_PATH}/../lib/* ${ip_addr}:${REMOTE_LIB_PATH} >/dev/null
            ssh root@"${ip_addr}" "cd  ${REMOTE_EXECUTIVE_PATH}; chmod +x obtain_service_info.sh; bash obtain_service_info.sh"
        done
        bash obtain_service_info.sh
    else
        bash obtain_service_info.sh
    fi
    file_name_list=("data/hardware/hardware_info.log" "data/hardware/hardware_pcie.log" "data/hardware/hardware_cpu.log"
    "data/hardware/hardware_disk.log" "data/software/system_version.log")
    smartctl_file_name="data/hardware/hardware_smartctl.log"
    command_desc=("Server Model" "pci Info" "CPU Info" "Partition Drive" "Kernel Info")
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
    # Obtain Server Process Snapshot
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
        write_messages  e 0 5 "Failed to obtain the server process snapshot by invoking the ps -aux command."
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
  # Check System Log
  # Parameter 1: begnning time.
  # Parameter 1: ending time.
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
      write_messages   e 0 9 "Failed to obtain server logs."
      write_messages   s 0 9 "Failed to run sed -n '/${start_time}/,/${end_time}/p' /var/log/${SYS_LOG_} command."
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
    # Check whether the file exists.
    result_files=("data/hardware/hardware_cpu.log"  "data/hardware/hardware_disk.log"
    "data/hardware/hardware_info.log"  "data/hardware/hardware_pcie.log"
    "data/product/product_name.log" "data/software/system_version.log"
    "data/test/performance/test_perf_cpu_1.log" "data/test/performance/test_perf_disk_1.log"
    "data/test/performance/test_perf_mem_1.log"  "data/test/power/test_power_1.log"
    "data/test/performance/test_perf_net_1.log" "data/test/compatiable/test_perf_cpu_0.log"
    "data/test/compatiable/test_perf_cpu_1.log" "data/test/compatiable/test_perf_disk_0.log"
    "data/test/compatiable/test_perf_disk_1.log" "data/test/compatiable/test_perf_mem_0.log"
    "data/test/compatiable/test_perf_mem_1.log" "data/test/compatiable/test_perf_net_0.log"
    "data/test/compatiable/test_perf_net_1.log" "data/test/power/test_power_0.log")
    for data_file in "${result_files[@]}"; do
        if [[ ! -f "${data_file}" ]]; then
            write_messages  e 0 10 "The log file ${data_file} does not exist in the directory."
        fi
    done
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            for data_file in "${result_files[@]}"; do
                data_file=${data_file}_${ip_addr}
                if [[ ! -f "${data_file}" ]]; then
                    write_messages  e 0 10 "The log file ${data_file} does not exist in the directory."
                fi
            done
        done
    fi

    record_log_file=( "${error_file}" "${app_log_file}" "${log_file}")
    for file in "${record_log_file[@]}"; do
        if [[ -f ./log/"${file}" ]]; then
            cp ./log/"${file}" ./data/others/
        else
            write_messages  e 0 10 "The ${file} file does not exist in the ./log/ directory."
        fi
    done
    if [[ -f "${config_file}" ]]; then
        cp "${config_file}" ./data/
    fi
    if ! tar -czf log_"${current_time}".tar.gz data; then
        write_messages   e 0 10 "An error occurred when compressing the file."
    fi
    write_messages  c  34  10 "After the collection is complete, the log package log_${current_time}.tar.gz is stored in $CURRENT_PATH."
    IFS=${OLD_IFS}
}

check_error() {
    # Check whether any exception occurs during the collection.
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    for ((i = 0; i < ${#product_result_array[@]}; i++)); do
        if [[ "${product_result_array[i]}" = 'False' ]]; then
            index=$(($i/5))
            sub_index=$(($i%5))
            if [[ ${index} -eq 0 ]]; then
                desc="this server"
            else
                desc=${ip_list[$(($index-1))]}
            fi
            case ${sub_index} in
                0) write_messages   e 0 10 "Failed to obtain the server model by running the command from ${desc}." ;;
                1) write_messages   e 0 10 "Failed to run the pcie command from ${desc}." ;;
                2) write_messages   e 0 10 "Failed to run the lscpu command from ${desc}." ;;
                3) write_messages   e 0 10 "Failed to run the lsblk command from ${desc}." ;;
                4) write_messages   e 0 10 "Failed to obtain information about the operation system kernel version from ${desc}." ;;
            esac
        fi
    done
    if [[ "${system_message_result}" = 'False' ]]; then
        write_messages   e 0 10 "Failed to check system logs. Check whether you have permission to query /var/log/${SYS_LOG_}."
    fi
    for ((i = 0; i < ${#snapshot_result_array[@]}; i++)); do
        if [[ "${snapshot_result_array[i]}" = 'False' ]];then
            if [[ ${i} -eq 0 ]]; then
                write_messages   e 0 10 "Failed to query the process by running the ps command on this server."
            else
                index=$(( $i -1))
                write_messages   e 0 10 "Failed to query the process by running the ps command on ${ip_list[index]}"
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

# In the cluster environment, check whether the IP addresses can communicate with each other and obtain the operating system version.
check_connection_and_OS() {
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a ip_list <<< "$cluster_ip_lists"
    if [[ "${HAS_CLUSTER_ENV}" -eq 1 ]]; then
        for ip_addr in  "${ip_list[@]}"; do
            ssh root@"${ip_addr}" "mkdir -p  ${REMOTE_EXECUTIVE_PATH};mkdir -p ${REMOTE_LIB_PATH};"
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "${ip_addr} has not established a password-free mutual trust with the current server. Configure the mutual trust and re-execute the script."
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
    write_messages  i 0 1 "The current OS version is ${O_S_VERSION}."
    IFS="${OLD_IFS}"
}

# Ensure that the service application software, test tools, and dependent software are installed.
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
                write_messages e 0 1 "Failed to install the dependencies at ${ip_addr}. You can log in to the server and check for the cause on ${REMOTE_EXECUTIVE_PATH}/log/info.log"
                exit
            fi
        done
        bash env_preparation.sh
        if [[ $? -ne 0 ]]; then
             write_messages e 0 1 "Failed to install the dependency. Check for the cause on ${REMOTE_EXECUTIVE_PATH}/log/info.log"
             exit
        fi

    else
        bash env_preparation.sh
        if [[ $? -ne 0 ]]; then
             write_messages e 0 1 "Failed to install the dependency. Check for the cause on ${REMOTE_EXECUTIVE_PATH}/log/info.log"
             exit
        fi

    fi
    IFS="${OLD_IFS}"
}

HCS8_PREFIX="172.36.0.10:58089/rest/v2/virtualMachine/verifyIsHCS?mac="
HCS803_PREFIX="173.64.11.52:58088/rest/v2/virtualMachine/verifyIsHCS?mac="
check_physical_system(){
    # Check whether the current system runs on a physical server.
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
    if echo "${CURRENT_SYS_}" |grep -i "virtual" ; then
        CURRENT_MAC=$(dmidecode -s system-uuid)
        SYSTEM_UUID=$(dmidecode -s system-uuid)
        HCS_CHECK_FLAG=$(curl --connect-timeout 5 ${HCS8_PREFIX}${CURRENT_MAC}\&serverId=${SYSTEM_UUID} 2> /dev/null)

        if [[ ${HCS_CHECK_FLAG} == "" ]]; then
          HCS_CHECK_FLAG=$(curl --connect-timeout 5 ${HCS803_PREFIX}${CURRENT_MAC}\&serverId=${SYSTEM_UUID} 2> /dev/null)
        fi

        if [[ ${HCS_CHECK_FLAG} == "" ]]; then
            write_messages i 0 1 "KVM type: Non-flagship store HCS VMs"
        else
            write_messages i 0 1 "KVM type: ${HCS_CHECK_FLAG}"
        fi
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
    # Check whether the usage of the CPU, memory, drive and NIC is too high. If the usage is too high, a message will be
    # displayed indicating that the current environment is no idle.
    write_messages  i 0 3 "Environment self-check."
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
                    write_messages e 0 1 "The environment of the server ${ip_addr} is not idle. Stop the running services and log in to the server to check details on ${REMOTE_EXECUTIVE_PATH}/log/info.log."
                    ENV_NOT_IDLE=1
                fi
            done
            bash env_inspectation.sh
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "The environment on this server is not idle. Stop the running services and log in to the server to check details on ${REMOTE_EXECUTIVE_PATH}/log/info.log."
                ENV_NOT_IDLE=1
            fi
        else
            bash env_inspectation.sh
            if [[ $? -ne 0 ]]; then
                write_messages e 0 1 "The environment is not idle. Stop the running services and log in to the server to check details on ${REMOTE_EXECUTIVE_PATH}/log/info.log."
                ENV_NOT_IDLE=1
            fi
        fi
        if [[ "${ENV_NOT_IDLE}" -eq 1 ]] ; then
            write_messages  c  34 3 "The current environment is not idle. Confirm that no service applications and dependency is running and enter Y to continue."
            read -r SYS_IS_CLEAN
            write_messages  c  34 3 "You've entered ${SYS_IS_CLEAN} "
            if [[ "${SYS_IS_CLEAN}" == "Y"  ||  "${SYS_IS_CLEAN}" == "y" ]]; then
                exam_time=$((exam_time + 1))
            else
                exam_time=$((exam_time + 1))
            fi
        else
            write_messages  i 0 3 "The environment self-check is complete."
            break
        fi
    done
    if [[ "${exam_time}" -eq 5 ]]; then
        write_messages   e 0 3 "The environment self-check did not pass. Please ensure that all services \
and their dependencies have been stopped and then re-run the script. If you need to skip the \
environment self-check, please enter Y. Be careful that skipping the environment self-check may \
result in inaccurate test results. To exit the self-check program, please enter N."
        read -r INPUT_
        write_messages  e 0 3 "You've entered ${INPUT_} "
        if [[ "${INPUT_}" == "Y" ||  "${INPUT_}" == "y" ]]; then
           write_messages  i 0 3 "Environment self-test skipped"
        else
           exit
        fi
    fi
    IFS="${OLD_IFS}"
}

port_scan() {
    # Security scan: Run the NMAP command to scan listening ports.
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
    write_messages  i 0 6 "The port security test is complete."
    IFS="${OLD_IFS}"
}

validated_clamav_scan(){
    if [[ "${CLAMAV_SCAN}" -eq 1 ]]; then
        write_messages  i 0 6 "Perform antivirus scanning test now."
        scan_path_parser 1
        write_messages  i 0 6 "Antivirus scan completed."
    fi
}


validated_cvecheck(){
    if [[ "${CVE_SCAN}" -eq 1 ]]; then
        # Use CVE for vulnerability scan
        write_messages  i 0 6 "Perform vulnerability scanning test now."
        scan_path_parser 2
        write_messages  i 0 6 "Vulnerability scan completed."
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
    # Check whether the service application exists.
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
        ps_result=$(pgrep -lf "${process}")
    fi
    if [[ "${ps_result}" != '' ]]; then
        ps_results="${ps_results}"";""${ps_result}"
    fi
    echo "${ps_results}"
    IFS="${OLD_IFS}"
}

check_process_exits_stop() {
    # Check whether the service application exists. If so, stop the process.
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
            write_messages  i 34 "${step}" "The ${process} process exists."
            STOP_APP_FLAG=1
        else
            if [[ ${step} -ne  4 ]]; then
                write_messages  i 34 "${step}" "The ${process} process does not exist."
            fi
        fi
    done
    if [[ "${step}" -eq 9   &&  "${STOP_APP_FLAG}" -eq 0 ]] ; then
        for stop_comm in  "${stop_comm_list[@]}"; do
            eval "${stop_comm}" >>  "${CURRENT_PATH}"/log/"${app_log_file}"  2>&1
            if [[ "$?" -ne 0 ]]; then
                cd "${CURRENT_PATH}"||exit
                write_messages  e 0 "${step}" "An error occurred when running the ${stop_comm} command."
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
                write_messages  e 0 "${step}" "An error occurred when running the ${stop_comm} command."
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
                    write_messages  e 0 "${step}" "After running the ${stop_comm} command, wait for ${sleep_time} seconds. The ${process} process exists."
                    kill_process ${process}
                    check_times=$((check_times -1))
                else
                    if [[ ${step} -ne  4 ]]; then
                        write_messages  i 34 "${step}" "The ${process} process does not exist."
                    fi
                    break
                fi
            done
            ps_result="$(check_process ${process})"
            if [[ ${check_times} -le 0 ]] ; then
                write_messages  e 0 "${start_step}" "Failed to stop the service application ${process}. Check the stop script."
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
            while [[ "${ps_result}" != "" ]] && [[ ${check_times} -gt 0 ]]; do
                write_messages  c 31 "${step}" "The application is still being started. Stop the application and press any key to continue."
                read -r process_is_on
                ps_result="$(check_process ${process})"
                check_times=$((check_times -1))
            done
            if [[ ${check_times} -le 0 ]]; then
                write_messages  e 0 "${step}" "The application is still being started and fails to be stopped. Stop the application and then run the script."
                exit
            else
                write_messages  i 0 "${step}" "The ${process} process does not exist."
            fi
        done
    fi
    IFS="${OLD_IFS}"
}

kill_process() {
    # kill process
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
            echo -ne "\r Remaining time: ${min}:${se}\r"
            sleep 1
        done
    ) &
}



get_performance() {
    # Collect indicator data related to the CPU, memory, drive, NIC, and power consumption.
    # Parameter 1: collection interval
    # Parameter 2: collection duration
    # Parameter 3: collection sequence. The value 0 indicates collection before the compatibility test, 1 indicates collection during the performance test, and 2 indicates collection after the compatibility test
    # Parameter 4: step
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
    desc_array=('Collect data before the compatibility test' 'Collect data during the performance test' 'Collect data after the compatibility test')
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
    write_messages  i 0 "${step}" "${desc_array[${seq}]} has been finished."
    IFS="${OLD_IFS}"
}

start_app(){
    # Start the application
    start_step=$1
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
            if  ! eval "${start_comm}" >> "${CURRENT_PATH}"/log/"${app_log_file}" 2>&1 ;then
                cd "${CURRENT_PATH}"||exit
                write_messages  e 0 "${start_step}" "An error occurred when running the ${start_comm} command. Manually run the command to start the application."
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
                      write_messages  i 0 "${start_step}" "Succeeded in starting the application ${process}."
                      break
                else
                    check_times=$((check_times -1))
                fi
            done
            if [[ ${check_times} -le 0 ]]; then
                write_messages  e 0 "${start_step}" "Failed to start the application ${process}. Check the startup script."
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
                start_times=5
                if [[ "${ps_result}" != "" ]]; then
                    write_messages  c 34 "${start_step}" "Succeeded in starting the application ${process}."
                fi
                while [[ ${start_times} -gt 0 &&  "${ps_result}" == "" ]]
                do
                    # Manually start the application
                    HAND_START_APP=1
                    write_messages  c 31 "${start_step}" "Manually start the application ${process}. If it is started successfully, type \"Y\". Otherwise, type \"N\" :"
                    read -r process_is_on
                    write_messages i 0 "${start_step}" "You've entered ${process_is_on} "
                    if [[ "${process_is_on}" == "Y"  ||  "${process_is_on}" == "y" ]]; then
                        sleep "${SLEEP_TIME}"
                        ps_result="$(check_process ${process})"
                        if [[ "${ps_result}" != "" ]]; then
                        write_messages  c 34 "${start_step}" "Manual startup of the application ${process} is complete."
                        break
                    else
                        write_messages  e 0  "${start_step}" "The application process ${process} does not exist. Please try again."
                        start_times=$((start_times -1))
                    fi
                    else
                        start_times=$((start_times -1))
                    fi
                done
                if [[ ${start_times} -le 0 ]]; then
                    write_messages  e 0 "${start_step}" "The application failed to be started. Check the application and try again."
                    exit
                fi
            done
        fi
    fi
    IFS="${OLD_IFS}"
}

start_performance_test(){
    # Invoke the pressure test command to perform the pressure test.
    PERF_TEST_FLAG=0
    write_messages  i 0 7 "Start the pressure test tool"
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
                write_messages  e 0 7 "Failed to start the test tool by invoking the ${start_script} command. Manually start the test tool."
                HAND_START_TEST=1
                PERF_TEST_FLAG=0
            else
                performance_test_pid_array[$i]="${PID}"
                PERF_TEST_FLAG=1
            fi
        done
        IFS="${OLD_IFS}"
    fi
    start_times=5
    if [[ ${TEST_TOOL_COMM} -eq 1  ||  ${PERF_TEST_FLAG} -eq 0 ]];then
        while [[ ${start_times} -gt 0 ]]
        do
            write_messages  c 31 7  "Manually start the pressure test tool. If it is started successfully, type \"Y\". Otherwise, type \"N\":"
            read -r performance_is_on
            if [[ "${performance_is_on}" == "Y"  ||  "${performance_is_on}" == "y" ]]; then
                write_messages  c 31 7 "You've entered ${performance_is_on} "
                break
            else
                write_messages  c 31 7 "You've entered ${performance_is_on}"
                start_times=$((start_times -1))
            fi
        done
    fi
    if [[ ${start_times} -le 0 ]]; then
        write_messages  e 0 5 "Pressure test requires you to manually increase the pressure or start the pressure test tool. Confirm that you want to perform the pressure test before starting the pressure test tool."
        exit
    fi
}

reliablity_test(){
    # Reliability test
    OLD_IFS="${IFS}"
    IFS=','
    read -r -a app_list <<< "$application_names"
    read -r -a start_comm_list <<< "$start_app_commands"
    SLEEP_TIME=20
    RELIABLE_TEST_FLAG=0
    if [[ ${HAND_START_TEST} -eq 1 || ${TEST_TOOL_COMM} -eq 1 ]];then
        write_messages  c 31 8 "Reliability test: The pressure test tool has been manually started. As the pressure test is complete, stop the pressure test tool and press any key to continue."
        read -r process_is_on
        write_messages  c 31 8 "You've entered ${process_is_on}"
    else
        for pid in "${performance_test_pid_array[@]}"
        do
	        # All subprocesses of the pid
            sub_pids=$(pgrep -P ${pid})
            for subpid in ${sub_pids};
            do
                kill -9  "${subpid}" >>./log/"${log_file}"  2>&1
            done
            disown "${pid}"
            kill -9  "${pid}" >>./log/"${log_file}"  2>&1
            if [[ $? -ne 0 ]] ; then
                write_messages   e 0 8 "Failed to stop the pressure test tool. Manually stop it. Press any key to continue."
                read -r process_is_on
                write_messages  c  31 8 "You've entered ${process_is_on}"
                continue
            fi
            sleep "${SLEEP_TIME}"
        done
    fi
    if [[ ${KUBERNETES_ENV} -eq 0  &&  ${HAND_START_APP} -eq 0 && ${START_APP_COMM} -eq 0 ]];then
        length=${#app_list[@]}
        for ((i = 0; i < "${length}"; i++));
        do
            process=${app_list[$i]}
            process=$(echo ${process} | awk '{gsub(/^\s+|\s+$/, "");print}')
            ps_result="$(check_process ${process})"
            if [[ "${ps_result}" != "" ]]; then
                write_messages  i 0 8 "Before performing reliability test, check whether the application process ${process} exists."
                if ! kill_process ${process} >>./log/"${log_file}"  2>&1 ; then
                    write_messages  e 0 8 "Reliability test: An error occurred when forcibly killing the ${process} process. The reliability test failed."
                    RELIABLE_TEST_FLAG=1
                else
                    write_messages  i 0 8 "Reliability test: The ${process} process was forcibly killed."
                fi
            else
                write_messages  e 31 8 "Before the reliability test, the application ${process} has been stopped. The reliability test failed."
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
                    write_messages  i 0 8 "Reliability test: The startup of the ${process} process is complete. The reliability test is successful."
                else
                    write_messages  e 31 8 "Reliability test: The reliability test failed because the application ${process} failed to be started."
                fi
            done
        fi
    elif [[  ${KUBERNETES_ENV} -eq 1 ]]; then
        write_messages  c 31 8 "This test case is not supported because the application is kubernetes cluster."
    else
        write_messages  c 31 8 "This test case is not supported because the application was manually started."
    fi
    IFS="${OLD_IFS}"
}


notice_users
production_env_waring

if [[ "$(id -u)" -ne 0 ]]; then
  write_messages  c 31 0 "Run the script as the root user. Otherwise, you do not have the permission to query system logs."
fi

# 1. Check whether the configuration file is correct.
write_messages  c  34 1 "Step 1: Start checking the configuration file."
check_configuration
write_messages  c  34 1 "Step 1: Finish checking the configuration file."
# Initialize the system and create a collection directory tree.
write_messages  c 34  1 "Step 2: Start checking the software dependency."
create_result_dir

# Prepare the environment and check the user environment.
env_preparation
check_physical_system
# Check whether the Kunpeng DevKit exists. If so, stop the it.
stop_or_start_kunpengdeveloper 1

message_start_time="$(tail -1 /var/log/${SYS_LOG_}| awk -F' ' '{for(i=1;i<=3;i++) printf $i OFS}')"
write_messages  c  34 1 "Step 2: Finish checking the software dependency."

# 2. Perform environment self-check.
write_messages  c  34 3 "Step 3: Start checking the test environment."
sys_env_inspectation
write_messages  c  34 3 "Step 3: Finish checking the test environment."

write_messages  c  34 4 "Step 4: Collect data related to system resources, such as CPU, memory, drive, NIC, and power consumption, before starting the application."
# 3. Collect system information.
get_service_info

# 4. Collect indicator data before starting the application.
# 4.1 Check for the application process. If the process exists, stop it.
check_process_exits_stop 4
# 4.2 Collect indicator data
get_performance 5 2 0 4
write_messages  c  34 4 "Step 4: Finish the collection before the application is started."
sleep 10
# 5. Start the application.
write_messages  c  34 5 "Step 5: Start the application. You can view the progress on the info.log file in the ./log directory. \
If the next step is pending for a long time, check whether the startup script in the configuration file is executed with output returned."
start_app 5
get_ps_snapshot
write_messages  c  34 5 "Step 5: Finish starting the application."

# Perform security check.
write_messages  c  34 6 "Step 6: Perform a security test by scanning the application's port. You can view the progress in the nmap.log file in the ./log directory."
port_scan
validated_clamav_scan
validated_cvecheck
write_messages  c  34 6 "Step 6: Finish the security test."

# 7.1 Perform the pressure test.
write_messages  c 34 7 "Step 7: Collect data related to system resources such as CPU, memory, drive, and NIC under service pressure."
start_performance_test

# 7.2 Collect data for the pressure test.
sleep 2
get_performance 5 5 1 7
sleep 20
write_messages  c  34 7 "Step 7: Finish collecting the data for the pressure test."


# 8. Perform the reliability test
write_messages  c  34 8 "Step 8: Perform a reliability test. Start the application after it is forcibly killed."
reliablity_test
write_messages  c  34 8 "Step 8: Finish the reliability test."

# 9. Collect indicator data after the application is stopped.
write_messages  c  34 9 "Step 9: Collect data related to system resources, such as CPU, memory, drive, NIC, and power consumption, after the application is stopped."
check_process_exits_stop 9
get_performance 5 2 2 9
sleep 10
write_messages  c  34 9 "Step 9: Finish collecting the data after the application is stopped."

write_messages  c  34 10 "Step 10: Package the collected test data."
stop_or_start_kunpengdeveloper 10
message_end_time="$(tail -1 /var/log/${SYS_LOG_}| awk -F' ' '{for(i=1;i<=3;i++) printf $i OFS}')"
check_system_message "${message_start_time}" "${message_end_time}"
# Check whether any error occurs during the preceding collections.
check_error
tar_output
exit
