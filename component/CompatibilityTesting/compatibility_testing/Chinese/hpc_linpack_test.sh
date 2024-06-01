#!/bin/bash
##################################
#HPC 基础性能测试-Linpack 计算性能测试
#版本信息: 华为技术有限公司，版权所有（C） 2020-2022
#修改记录：2022-03-02 修改
##################################

CURRENT_PATH=$(pwd)
LIB_PATH=${CURRENT_PATH}/../lib/hpc/
OPENMPI_PATH=/home/compatibility_testing/hpc/openMPI
OPENBALSE_PATH=/home/compatibility_testing/hpc/openBLAS


current_time=$(date "+%Y%m%d")
log_file=info.log_${current_time}
SYS_=0

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

    if ! gcc -v ; then
        write_messages e 31 10 "没有安装编译器套件GCC C 编译器，请安装GCC 9.3.0 的以上的版本。"
        exit 1
    else
        gcc_version=$(gcc --version |grep -Eo "[0-9]+\.[0-9]+\.[0-9]+"|head -1| awk -F'.' '{print $1}')
        if [[ "${gcc_version}" -lt 9 ]] ;then
            write_messages e 31 10 "请安装GCC 9.3.0 的以上的版本。"
            exit 1
        fi
    fi

    if ! g++ -v; then
        write_messages e 31 10 "没有安装编译器套件GCC C++ 编译器，请安装GCC 9.3.0 的以上的版本。"
        exit 1
    else
        cpp_version=$(g++ --version |grep -Eo "[0-9]+\.[0-9]+\.[0-9]+"|head -1| awk -F'.' '{print $1}')
        if [[ "${cpp_version}" -lt 9 ]]; then
            write_messages e 31 10 "请安装GCC 9.3.0 的以上的版本。"
            exit 1
        fi
    fi

    if ! gfortran -v; then
        write_messages e 31 10 "没有安装编译器套件GCC fortran 编译器，请安装GCC 9.3.0 的以上的版本。"
        exit 1
    else
        fortran_version=$(gfortran --version |grep -Eo "[0-9]+\.[0-9]+\.[0-9]+"|head -1| awk -F'.' '{print $1}')
        if [[ "${fortran_version}" -lt 9 ]]; then
            write_messages e 31 10 "请安装GCC 9.3.0 的以上的版本。"
            exit 1
        fi
    fi
}

install_openMPI() {
    tar_name="openmpi-4.0.3.tar.gz"
    tar_path=${LIB_PATH}"${tar_name}"

    check_os_version
    if ! mpirun --version ; then
        install_GCC ${SYS_}
        cd ${LIB_PATH} ||exit

        if [[ ! -e "${tar_name}" ]];then
            write_messages  e 31 10 "${tar_path}文件不存在，请检查下载的压缩包是否完整。"
            exit 1
        fi

        if ! tar -zxvf "${tar_name}" ;then
            write_messages  e 31 10 "解压${tar_path}文件时出错，请检查下载的压缩包是否完整。"
            exit 1
        fi

        cd openmpi-4.0.3/
        mkdir -p build
        cd build
        ../configure --prefix=${OPENMPI_PATH} --enable-pretty-print-stacktrace --enable-orterun-prefix-by-default \
        --with-cma --enable-mpi1-compatibility
        make && make install -j`nproc`

        sed -i -e '$a export PATH=$PATH:'${OPENMPI_PATH}'/bin' /etc/profile
        sed -i -e '$a export LD_LIBRARY=$LD_LIBRARY:'${OPENMPI_PATH}'/lib' /etc/profile
        sed -i -e '$a export INCLUDE=$INCLUDE:'${OPENMPI_PATH}'/include' /etc/profile
        source /etc/profile

        cd "${CURRENT_PATH}"||exit
        if ! mpirun --version ; then
            write_messages e 31 10 "安装openMPI失败，请手动安装。"
            exit
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

install_openBLAS(){
    tar_name="OpenBLAS-0.3.6.tar.gz"
    tar_path=${LIB_PATH}"${tar_name}"

    cd ${LIB_PATH} ||exit

    if [[ ! -e "${tar_name}" ]];then
        write_messages  e 31 10 "${tar_path}文件不存在，请检查。"
        exit 1
    fi

    if ! tar -zxvf "${tar_name}" ;then
        write_messages  e 31 10 "解压${tar_path}文件时出错，请检查。"
        exit 1
    fi

    cd OpenBLAS-0.3.6/ ||exit
    export CC=`which gcc`
    export CXX=`which g++`
    export FC=`which gfortran`
    make -j`nproc`
    make PREFIX=${OPENBALSE_PATH} install -j`nproc`

    sed -i -e '$a export PATH=$PATH:'${OPENBALSE_PATH}'/bin' /etc/profile
    sed -i -e '$a export LD_LIBRARY=$LD_LIBRARY:'${OPENBALSE_PATH}'/lib' /etc/profile
    sed -i -e '$a export INCLUDE=$INCLUDE:'${OPENBALSE_PATH}'/include' /etc/profile
    source /etc/profile


    cd ${CURRENT_PATH}|| exit
    if ! find ${OPENBALSE_PATH}/lib -name "*.so" ;then
        write_messages e 31 10 "安装OpenBLAS失败。"
        exit 1
    fi

}

install_HPL(){
    declare -A cores_array=(["24"]="4*6" ["32"]="4*8" ["48"]="4*12" ["64"]="8*8" ["96"]="8*12" ["128"]="8*16" ["192"]="8*24" ["256"]="16*16")
    tar_name="hpl-2.3.tar.gz"
    tar_path=${LIB_PATH}"${tar_name}"
    file_name=${CURRENT_PATH}/data/test/compatiable/test_hpc_linpack.log

    cd ${LIB_PATH} ||exit

    if [[ ! -e "${tar_name}" ]];then
        write_messages  e 31 10 "${tar_path}文件不存在，请检查。"
        exit 1
    fi

    if ! tar -zxvf "${tar_name}" ;then
        write_messages  e 31 10 "解压${tar_path}文件时出错，请检查。"
        exit 1
    fi

    cd hpl-2.3 ||exit
    cp -f ./../Make.Linux_Arm .
    sed -i -e 's:${HPL_PATH}:'${LIB_PATH}':g' -e 's:${OPENBLAS_PATH}:'${OPENBALSE_PATH}':g' ./Make.Linux_Arm
    make arch=Linux_Arm -j`nproc`

    cd ${CURRENT_PATH} ||exit
    if ! find ${LIB_PATH}/hpl-2.3/bin/Linux_Arm -name "HPL.dat" ;then
        write_messages e 10 1 "安装HPL失败。"
        exit 1
    else
        memory_size=$(cat /proc/meminfo |grep MemTotal|awk '{print $2}')
        ns_float=$(echo "sqrt(0.9 * $memory_size * 1024/8) * 0.8" |bc)
        ns=$(echo ${ns_float} |awk -F'.' '{print $1}')
        cpu_cores=$(lscpu |grep -E "^CPU(\(s\))?:"|awk -F':' '{print $2}')
        cpu_cores=$(echo ${cpu_cores})
        grip=${cores_array[$cpu_cores]}
        if [[ -z ${grip} ]];then
            ps=2
            qs=$(echo ${cpu_cores}/2|bc )
        else
            ps=$(echo ${grip} |awk -F'*' '{print $1}')
            qs=$(echo ${grip} |awk -F'*' '{print $2}')
        fi
        sed -i -e 's/${ns}/'${ns}'/g' -e 's/${ps}/'${ps}'/g'  -e 's/${qs}/'${qs}'/g' ${LIB_PATH}/HPL.dat
        cp -f ${LIB_PATH}/HPL.dat ${LIB_PATH}/hpl-2.3/bin/Linux_Arm/HPL.dat
        cd ${LIB_PATH}/hpl-2.3/bin/Linux_Arm/ ||exit
        sleep 60
        mpirun --allow-run-as-root -npernode ${cpu_cores} -hostfile ${LIB_PATH}/hostfile -x OMP_NUM_THREADS=1 ${LIB_PATH}/hpl-2.3/bin/Linux_Arm/xhpl 2>&1 |tee ${file_name}
        cd ${CURRENT_PATH} ||exit
    fi
}

remove_config(){

    sed -i -e 's;^export PATH=$PATH:'${OPENBALSE_PATH}'/bin;;g' /etc/profile
    sed -i -e 's;^export LD_LIBRARY=$LD_LIBRARY:'${OPENBALSE_PATH}'/lib;;g' /etc/profile
    sed -i -e 's;^export INCLUDE=$INCLUDE:'${OPENBALSE_PATH}'/include;;g' /etc/profile
    source /etc/profile
}


install_openMPI
install_openBLAS
echo 3 > /proc/sys/vm/drop_caches
install_HPL
remove_config