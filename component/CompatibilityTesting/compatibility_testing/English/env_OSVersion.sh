#!/bin/bash
##################################
#Function description: Check the Current OS Version
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
##################################

versions_osVersion() {
  # Check the Current OS Version
  os_version_='unrecognized'
  os_system_=$(uname -s)
  LSB_RELEASE='/etc/lsb-release'
  case ${os_system_} in
    Linux)
      if [[ -r '/etc/os-release' ]]; then
        os_version_=$(awk -F= '$1~/PRETTY_NAME/{print $2}' /etc/os-release \
                |sed 's/"//g')
        if  echo "${os_version_}"|grep -i "CentOS" &> /dev/null ; then
          if [[ -r '/etc/redhat-release' ]]; then
             os_version_=$(cat /etc/redhat-release)
          fi
        fi
      elif [[ -r '/etc/redhat-release' ]]; then
         os_version_=$(cat /etc/redhat-release)
      elif [[ -r '/etc/SuSE-release' ]]; then
                os_version_=$(head -n 1 /etc/SuSE-release)
      elif [[ -r "${LSB_RELEASE}" ]]; then
        if grep -q 'DISTRIB_DESCRIPTION' "${LSB_RELEASE}"; then
          # shellcheck disable=SC2002
          os_version_=$(cat "${LSB_RELEASE}" \
                    |awk -F= '$1~/DISTRIB_DESCRIPTION/{print $2}' \
                    |sed 's/"//g;s/ /-/g')
        fi
      fi
      ;;
    *)
      write_messages  e 0 1 "The tool can run only on the Linux operating system."
      exit
      ;;
  esac
  Kylin_Build=""
  if echo "${os_version_}"|grep -i -E "\<Kylin\>|\<NeoKylin\>" &> /dev/null; then
    if [[ -f /var/log/messages ]];then
        > /var/log/messages
    fi
    if hash nkvers 2>/dev/null; then
      Kylin_Build=$(nkvers|sed -n -e '/Build/,/^$/'p|grep -Ev "Build:|#+")
      Kylin_Build=$(echo ${Kylin_Build})
    fi
  fi

  uos_edition_name=""
  uos_edition_name_j=""
  uos_edition_name_v=""

  if echo "${os_version_}"|grep -E -i "\<UnionTech\>|\<UOS\>" &> /dev/null; then
    if [[ -f /var/log/messages ]];then
        > /var/log/messages
    fi
    if [[ -r '/etc/os-version' ]]; then
        uos_edition_name=$(cat /etc/os-version|grep 'EditionName\[zh_CN\]'|awk -F '=' '{print $NF}')
        uos_edition_name_j=$(cat /etc/os-version|grep 'MajorVersion'|awk -F '=' '{print $NF}')
        uos_edition_name_v=$(cat /etc/os-version|grep 'MinorVersion'|awk -F '=' '{print $NF}')
    fi
  fi


  if [[ "${Kylin_Build}"x != ""x ]];then
      os_version_=${Kylin_Build}
  fi
  if [[ "${uos_edition_name_v}"x != ""x ]]; then
      os_version_="UnionTech OS Server"" V""${uos_edition_name_j}"" ""${uos_edition_name_v}""e"
  fi
  echo "${os_version_}"
  unset  os_system_ os_version_

}

versions_osVersion