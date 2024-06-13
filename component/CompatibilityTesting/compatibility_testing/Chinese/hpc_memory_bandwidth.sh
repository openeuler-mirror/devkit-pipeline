#!/bin/bash
##################################
#HPC内存带宽测试
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-03-02 创建
##################################

if [[ ! -d "./log" ]]; then
  mkdir ./log
fi
CURRENT_PATH=$(pwd)
LIB_PATH=${CURRENT_PATH}/../lib/hpc/
SYS_=0
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
error_file=error.log_${current_time}


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

install_GCC(){
    sys_id=$1
    if [[ "${sys_id}" -eq 1 ]];then
        if ! gcc -v ; then
            if ! yum install -y gcc; then
                write_messages e 34 10 "安装编译器套件GCC C 编译器失败，请检查网络环境和yum源配置，并安装GCC的RPM包。"
            fi
        fi
    else
        if ! gcc -v ; then
            if ! apt-get install -y gcc; then
                write_messages e 34 10 "安装编译器套件GCC C 编译器失败，请检查网络环境和apt源配置，并安装GCC的RPM包。"
            fi
        fi
    fi
}

check_os_version(){
    if ! hash apt-get 2>/dev/null; then
        SYS_=1;
    else
        SYS_=2;
    fi
}


hpc_memory_bandwidth() {

    tar_name="STREAM-master.tar.gz"
    stream_path="${LIB_PATH}/STREAM-master/"
    file_path=/data/test/compatiable/

    cd ${LIB_PATH} ||exit
    if [[ ! -e "${tar_name}" ]];then
        write_messages  e 34 10 "${tar_name}文件不存在，缺少测试工具，请检查。"
        exit 1
    fi
    if ! tar -zxvf "${tar_name}";then
        write_messages  e 34 10 "解压文件时出错，请检查。"
        exit 1
    fi

    cd "${stream_path}" || exit 1

    write_messages  i 31 10 "开始安装测试工具。"
    if eval "make clean" >> "${CURRENT_PATH}"/log/"${log_file}" 2>&1 ;then
        eval "make" >> "${CURRENT_PATH}"/log/"${log_file}" 2>&1
        write_messages  i 31 10 "安装测试工具成功。"
    else
        write_messages  e 34 10 "HPC测试工具STREAM安装失败。"
        exit 1
    fi

    write_messages  i 31 10 "开始执行内存带宽测试。"
    if ! eval "echo never > /sys/kernel/mm/transparent_hugepage/enabled" >> "${CURRENT_PATH}"/log/"${log_file}" 2>&1;then
        write_messages  e 34 10 "HPC测试工具STREAM执行错误，请查看日志";
        exit 1
    fi

    if ! eval "echo never > /sys/kernel/mm/transparent_hugepage/defrag" >> "${CURRENT_PATH}"/log/"${log_file}" 2>&1;then
        write_messages  e 34 10 "HPC测试工具STEAM执行错误，请查看日志";
        exit 1
    fi

    file_name="test_hpc_memory_bandwidth_0.log"
    write_messages  i 31 10 "内存带宽工具执行测试"
    if ! ./stream_c.exe | sed -n '25,31p' >> "${CURRENT_PATH}""${file_path}""${file_name}" 2>&1; then
        write_messages  e 34 10 "HPC内存带宽测试失败"
	    exit 1
    fi
    eval "echo 3 > /proc/sys/vm/drop_caches"
    write_messages  i 31 10 "HPC内存带宽测试成功。"
}

install_GCC
hpc_memory_bandwidth