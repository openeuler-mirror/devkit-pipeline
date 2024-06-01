#!/bin/bash
##################################
#功能描述: 测试环境的操作系统版本检查
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-02-10 修改
##################################

# 创建日志目录
versions_osVersion() {
  # 当前系统版本检查
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
      write_messages  e 0 1 "工具仅支持Linux系统"
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