#!/bin/bash
##################################
#Function description: Check Whether Kunpeng Devkit is running. If yes, stop it during the test.
#Version information: Copyright © Huawei Technologies Co., Ltd. 2020–2022
#Change history: Modified on 2022-02-10
##################################

# Create a log directory.
if [[ ! -d "./log" ]]; then
  mkdir ./log
fi
CURRENT_PATH=$(pwd)
current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
HAS_KUNPENG_DEVKIT=0

stop_or_start_kunpengdeveloper(){
    step=$1
    kit_list=("tuning_kit" "depende+" "porting" "hyper_tuner")
    kit_service_list=("thor.service" "nginx_port.service" "gunicorn_port.service")
    for kit in "${kit_list[@]}"; do
        ps_result="$(pgrep -lf "${kit}") "
        if [[ "${ps_result}" != ' ' ]]; then
            HAS_KUNPENG_DEVKIT=1
            break
        fi
    done
    if [[ ${HAS_KUNPENG_DEVKIT} -eq 1  &&  ${step} -eq 1 ]];then
        for service in "${kit_service_list[@]}"; do
            eval "systemctl stop ${service}" >>  "${CURRENT_PATH}"/log/"${log_file}"  2>&1
        done
    fi
    if [[ ${HAS_KUNPENG_DEVKIT} -eq 1  &&  ${step} -eq 10 ]];then
        for service in "${kit_service_list[@]}"; do
            eval "systemctl start ${service}" >>  "${CURRENT_PATH}"/log/"${log_file}"  2>&1
        done
    fi
}

stop_or_start_kunpengdeveloper $1
