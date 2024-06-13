#!/bin/bash
##################################
#Function description: Check and install dependencies of the tool.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
##################################

# Create a log directory.
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



env_preparation() {
  # Ensure that the service application software, test tools, and dependent software are installed.
  write_messages  i 0 1 "Ensure that the service application software, test tools, and dependent software are installed."
  software_list=(nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sar bc)
  rpm_list=(nmap ipmitool dmidecode pciutils util-linux util-linux net-tools net-tools sysstat bc)
  deb_list=(nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sysstat bc)
  software_des=('Vulnerability Scan' 'Power Consumption Test' 'Check Hardware Info' 'Check PCI Bus' 'Check CPU Info' 'Check Drive Partitions' 'Check NIC' 'Number of Network Connections' 'Performance Analysis' 'Floating-Point Computing'  )
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
        write_messages  i 0 1 "Installing the ${software_desc} software ${software_app}... Please wait."
        if ! yum -y install "${rpm_app}"; then
          write_messages  e 0 1 "Failed to install the ${software_desc} software ${software_app}. Check whether the Internet is accessible and the yum source is configured, and install the RPM packages of nmap, ipmitool, dmidecode, net-tools, pciutils, util-linux, sysstat."
          exit 1
        fi
      else
        write_messages  i 0 1 "The ${software_desc} software has been installed."
      fi
    done
  elif [[ "${sys_id}" -eq 2 ]];then
    for ((i = 0; i < "${length}"; i++)); do
      software_app=${software_list[$i]}
      software_deb=${deb_list[$i]}
      software_desc=${software_des[$i]}
      if ! hash "${software_app}" 2>/dev/null; then
        write_messages  i 0 1 "Installing the ${software_desc} software ${software_app}... Please wait."
        if ! apt -y install "${software_deb}"; then
          write_messages  e 0 1 "Failed to install the ${software_desc} software ${software_app}. Check whether the Internet is accessible and the apt source is configured, and install the DEB packages nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sysstat bc."
          exit 1
        fi
      else
        write_messages  i 0 1 "The ${software_desc} software has been installed."
      fi
    done
  elif [[ "${sys_id}" -eq 3 ]];then
    for ((i = 0; i < "${length}"; i++)); do
      software_app=${software_list[$i]}
      software_deb=${deb_list[$i]}
      software_desc=${software_des[$i]}
      if ! hash "${software_app}" 2>/dev/null; then
        write_messages  i 0 1 "Installing the ${software_desc} software ${software_app}... Please wait."
        if ! zypper install -y "${software_deb}"; then
          write_messages  e 0 1 "Failed to install the ${software_desc} software ${software_app}. Check whether the Internet is accessible and the zypper source is configured, and install the DEB packages nmap ipmitool dmidecode lspci lscpu lsblk ifconfig netstat sysstat bc."
          exit 1
        fi
      else
        write_messages  i 0 1 "The ${software_desc} software has been installed."
      fi
    done
  else
    write_messages  c 31 1 "Currently, only the released OS versions of CentOS, Redhat, NeoKylin, Ubuntu, Kylin, UOS and openEuler are supported."
    exit 1
  fi
}

smartctl_install(){
    # install smartctl
    if  hash yum  2>/dev/null && ! hash smartctl 2>/dev/null; then
        write_messages  i 0 1 "Installing the Control and Monitor Utility for SMART Disks software ... Please wait."
        if ! yum install -y smartmontools; then
            write_messages  e 0 1 "Failed to install the Control and Monitor Utility for SMART Disks software. Check whether the Internet is accessible and the yum source is configured, and install the RPM packages of smartmontools"
            exit 1
        fi
    fi
}

env_preparation
smartctl_install








