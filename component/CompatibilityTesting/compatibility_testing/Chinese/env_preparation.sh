#!/bin/bash
##################################
#功能描述: 工具的依赖软件检查和安装
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-02-10 修改
##################################

# 创建日志目录
source ~/.bashrc
shopt -s expand_aliases
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



env_preparation() {
  # 检查系统环境准备情况
  write_messages  i 0 1 "请用户确保安装业务应用软件、测试工具及其依赖软件。"
  software_list=(nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sar bc)
  rpm_list=(nmap ipmitool dmidecode pciutils util-linux util-linux net-tools net-tools sysstat bc)
  deb_list=(nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sysstat bc)
  software_des=('漏洞扫描' '功耗测试' '查看硬件信息' '查看PCI总线' '查看CPU信息' '查看硬盘分区' '查看网络接口' '网络连接数' '性能分析' '浮点计算'  )
  suse_sys=(SuSE)
  length=${#software_list[@]}
  sys_id=0
  os_version=$(bash ${CURRENT_PATH}/env_OSVersion.sh)

  if ! hash apt-get 2>/dev/null; then
    sys_id=1;
	SYS_LOG_="messages"
  else
    sys_id=2;
	SYS_LOG_="syslog"
  fi
  for item in "${suse_sys[@]}"; do
      if echo "${os_version}"|grep -i "${item}" &> /dev/null; then
        sys_id=3;
        SYS_LOG_="messages"
      fi
  done
  if [[ "${sys_id}" -eq 1 ]];then
    for ((i = 0; i < "${length}"; i++)); do
      software_app=${software_list[$i]}
      rpm_app=${rpm_list[$i]}
      software_desc=${software_des[$i]}
      if ! hash "${software_app}" 2>/dev/null; then
        write_messages  i 0 1 "现在安装${software_desc}软件${software_app},请稍等。"
        if ! yum -y install "${rpm_app}"; then
          write_messages  e 0 1 "安装${software_desc}软件${software_app}失败，请检查网络环境和yum源配置，并安装nmap,ipmitool,dmidecode,net-tools,pciutils,util-linux,sysstat的RPM包。"
          exit 1
        fi
      else
        write_messages  i 0 1 "${software_desc}软件已安装"
      fi
    done
  elif [[ "${sys_id}" -eq 2 ]];then
    for ((i = 0; i < "${length}"; i++)); do
      software_app=${software_list[$i]}
      software_deb=${deb_list[$i]}
      software_desc=${software_des[$i]}
      if ! hash "${software_app}" 2>/dev/null; then
        write_messages  i 0 1 "现在安装${software_desc}软件${software_app},请稍等。"
        if ! apt -y install "${software_deb}"; then
          write_messages  e 0 1 "安装${software_desc}软件${software_app}失败，请检查网络环境和apt源配置，并安装nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sysstat bc的deb包。"
          exit 1
        fi
      else
        write_messages  i 0 1 "${software_desc}软件已安装"
      fi
    done
  elif [[ "${sys_id}" -eq 3 ]];then
    for ((i = 0; i < "${length}"; i++)); do
      software_app=${software_list[$i]}
      software_deb=${deb_list[$i]}
      software_desc=${software_des[$i]}
      if ! hash "${software_app}" 2>/dev/null; then
        write_messages  i 0 1 "现在安装${software_desc}软件${software_app},请稍等。"
        if ! zypper install -y "${software_deb}"; then
          write_messages  e 0 1 "安装${software_desc}软件${software_app}失败，请检查网络环境和apt源配置，并安装nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sysstat bc的deb包。"
          exit 1
        fi
      else
        write_messages  i 0 1 "${software_desc}软件已安装"
      fi
    done
  else
    write_messages  c 31 1 "当前仅支持CentOS、Redhat、中标麒麟、Ubuntu、银河麒麟、UOS、openEuler 发行版本。"
    exit 1
  fi
}

smartctl_install(){
    # smartctl 软件安装
    if  hash yum  2>/dev/null && ! hash smartctl 2>/dev/null; then
        write_messages  i 0 1 "现在安装硬盘版本号查看软件smartctl,请稍等。"
        if ! yum install -y smartmontools; then
            write_messages  e 0 1 "安装硬盘版本号查看软件smartctl失败，请检查网络环境和yum源配置，并安装smartmontools的RPM包。"
            exit 1
        fi
    fi
}

env_preparation
smartctl_install








