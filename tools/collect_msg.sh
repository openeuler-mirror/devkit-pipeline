#!/bin/bash
current_path=$(pwd)
config_file=$current_path/config.ini
log_path=$current_path/log
default_project="Bigdata Database Storage Arm Virt Acclib Virtual HPC"





#####################根据section以及key值获取对应value############
acquire_value(){
    project=$1
    key=$2
    opt="{sub(/$key=/,\"\")}1"
    grep $project -A 100  $config_file |grep -m 1 "$key="|awk $opt
}


######################获取配置文件中选择校验的解决方案###########
acquire_select_project(){
    all_name=""
	for per_project in $default_project
	do
		status=$(acquire_value $per_project check)
		if [[ $status = True ]]; then
			all_name="$all_name $per_project"
        fi
	done
	echo $all_name
}


check_customer_info(){
  customer_information=$1
  if [  -z "$customer_information" ];
  then
    echo "请您在config.ini Global字段 填写唯一个人标识 eg: xxx有限公司"
    exit 1
  fi

  if [[ "$customer_information"x =~ "xxx有限公司" ]];
  then
    echo "请您在config.ini Global字段 修改 xxx有限公司为具体公司名称以供标识"
    exit 1
  fi
}


####################打包日志文件############################
tar_log_file(){
  customer_information=$1
  datatime=$(date '+%Y%m%d%H%M')

  if test -d $log_path;
  then
      echo "customer_information: ${customer_information}_isv_msg_${datatime}.tar.gz" >> $log_path/os_msg.log
      tar zcvf ${customer_information}_isv_msg_${datatime}.tar.gz -C $log_path .
  else
      echo "$log_path logpath abnormality, please check"
  fi
}


###################arm原生特性信息收集#################
collect_arm_native(){
     kbox_container=$1
     video_container=$2
     instruction_container=$3
     # check tested container whether exist
     containers=($1 $2 $3)
     for i in ${containers[@]};do
	     docker ps -a | grep -wq $i || { echo "the $i container doesn't found,please check!"; exit 1; }
     done
     # kbox基础云手机
     rm -f $log_path/arm_native.log

     keyword=(gralloc.kbox.so audio.primary.kbox.so gps.kbox.so sensors.kbox.so libmedia_omxcore.so libstagefrighthw.so vinput hwcomposer.kbox.so)
     docker exec -it $kbox_container lsof > $log_path/arm_native_raw.log
     for i in "${keyword[@]}";do
	     grep -F "${i}" $log_path/arm_native_raw.log >> $log_path/arm_native.log
     done
     rm -f $log_path/arm_native_raw.log

     docker exec -it $kbox_container cat /proc/sys/fs/binfmt_misc/ubt_a32a64 >> $log_path/arm_native.log
     # 视频流
     docker exec -it $video_container lsof | grep VmiInputFlinger >> $log_path/arm_native.log
     # 指令流
     docker exec -it $instruction_container ps -ef | grep -F "VmiAgent instruction" >> $log_path/arm_native.log
}


#################虚拟化特性信息收集##################
collect_virtual_host(){
    sudo systemctl status waasagent.service |grep "Active" > $log_path/virtual_sense.log
    waasctl --version >> $log_path/virtual_sense.log

    ovs_appctl_res=$(ovs-appctl --version 2>&1)

    if [[ $ovs_appctl_res =~ "command not found" ]];
    then
        echo "ovs-appctl: command not found" > $log_path/virtual_dpu_flow.log
    else
        echo "ovs-appctl version: $ovs_appctl_res" > $log_path/virtual_dpu_flow.log
		script -a -c 'ovs-appctl hwoff/dump-hwoff-flows' $log_path/virtual_dpu_flow.log
    fi
}


collect_virtual_dpu(){
    server_name=$1
    network=$2
    flavor=$3
    volume=$4
	availability_zone=$5

    # 需要再DPU测执行
    dpak_ovs_ctl_res=$(dpak-ovs-ctl -h 2>&1)
    if [[ $dpak_ovs_ctl_res =~ "command not found" ]];
    then
        echo "请确定已在DPU测执行该工具"
        echo "dpak_ovs_ctl: command not found" > $log_path/virtual_dpu.log
    else
        echo "dpak_ovs_ctl version: $dpak_ovs_ctl_res" > $log_path/virtual_dpu.log
		script -a -c 'dpak-ovs-ctl hwoff/dump-hwoff-flows' $log_path/virtual_dpu.log
    fi

	  /usr/libexec/spdk/scripts/hw_dpu_rpc.py get_version >> $log_path/virtual_dpu.log 2>&1
    /usr/libexec/spdk/scripts/hw_dpu_rpc.py get_controllers >> $log_path/virtual_dpu.log 2>&1

    # 创建虚机
    openstack_res=$(openstack --version 2>&1)

    if [[ $openstack_res =~ "command not found" ]];
    then
        echo "请确定已在DPU测执行该工具"
        echo "openstack: command not found" >> $log_path/virtual_dpu.log
    else
        echo "openstack version: $openstack_res" >> $log_path/virtual_dpu.log
        openstack server create $server_name --network $network --flavor $flavor --volume $volume --availability-zone $availability_zone >> $log_path/virtual_dpu.log
		echo "等待虚机创建完成"
        sleep 120
        echo "server_name: $server_name" >> $log_path/virtual_dpu.log
        openstack server list >> $log_path/virtual_dpu.log
    fi
}


collect_virtual_ceph(){
  vm_ip=$1
  vm_user=$2
  vm_password=$3
  vm_ceph_disk_name=$4
  vm_name=$5
  set -x
  sshpass -p "$vm_password" ssh "$vm_user"@"$vm_ip" ls -la /sys/block/ | grep $vm_ceph_disk_name | tail -n 1 > $log_path/virtual_ceph_disk.log
  virsh dumpxml $vm_name > $log_path/virtual_ceph_vm_xml.log
  rpc vhost_get_controllers > $log_path/virtual_ceph_vm_controllers.log
  rpc bdev_get_bdevs > $log_path/virtual_ceph_bdev.log
}


collect_virtual_ovs_xpf(){
  ovs-appctl hwoff/dump-hwoff-flows > $log_path/virtual_ovs_xpf.log
  echo $? >> $log_path/virtual_ovs_xpf.log
}


################数据库特性信息收集##################
collect_database(){
   mysql_install_path=$1
   mysql_username=$2
   mysql_password=$3
   mysql_port=$4
   database_name=$5
   nvme_name=$6
   plugin_path=$1/lib/plugin
   timeout=60
   $mysql_install_path/bin/mysqld_safe --defaults-file=/etc/my.cnf &
   # 循环判断进程是否启动
   for ((i=0; i<=$timeout; i++)); do
     if ps -ef | grep -v grep | grep "mysql" > /dev/null; then
       echo "Process mysql started."
       # 执行下一步操作
       sleep 5
       $mysql_install_path/bin/mysql -P $mysql_port -u $mysql_username -p$mysql_password -D $database_name -h127.0.0.1 -e "select * from INFORMATION_SCHEMA.plugins where PLUGIN_NAME like 'thread_pool%'" > $log_path/database_mysql.log
       $mysql_install_path/bin/mysql -P $mysql_port -u $mysql_username -p$mysql_password -D $database_name -h127.0.0.1 -e "select * from INFORMATION_SCHEMA.plugins where PLUGIN_NAME like 'kovae%'" >> $log_path/database_mysql.log
       echo thread_pool: $(ls $plugin_path |grep thread_pool.so) >> $log_path/database_mysql.log
       echo kovae_path: $(ls $plugin_path |grep ha_kovae.so) >>  $log_path/database_mysql.log
       readelf -a $mysql_install_path/bin/mysqld|grep bolt >> $log_path/database_mysql.log
       echo no_lock: $(objdump -d $mysql_install_path/bin/mysqld|grep -c row_vers_build_for_semi_consistent_readP5trx_t) >> $log_path/database_mysql.log
       objdump -d $mysql_install_path/bin/mysqld |grep crc32cb >> $log_path/database_mysql.log
       $mysql_install_path/bin/mysql -P $mysql_port -u $mysql_username -p$mysql_password -D $database_name -h127.0.0.1 -e "show variables like '%paralle%'" >> $log_path/database_mysql.log
       nm $mysql_install_path/bin/mysqld | grep -c Page_shards >> $log_path/database_mysql_page_shards.log
       $mysql_install_path/bin/mysql -P $mysql_port -u $mysql_username -p$mysql_password -D $database_name -h127.0.0.1 -e "show variables like 'sched_affinity%'" >> $log_path/database_mysql.log
       hioadm atomicwrite -d $nvme_name > $log_path/database_mysql_nvme.log
       $mysql_install_path/bin/mysql -P $mysql_port -u $mysql_username -p$mysql_password -D $database_name -h127.0.0.1 -e "show variables like '%flush_method%'" >> $log_path/database_mysql_nvme.log
       $mysql_install_path/bin/mysql -P $mysql_port -u $mysql_username -p$mysql_password -D $database_name -h127.0.0.1 -e "show variables like '%doublewrite%'" >> $log_path/database_mysql_nvme.log
       gazellectl lstack show 1 -c | grep ":$mysql_port" > $log_path/database_mysql_gazelle.log
       pkill -9 mysql
       break
     fi
     sleep 1
     if [ $timeout -eq $i ];then
       echo "Timeout error: mysql process not started in $timeout seconds.i"
       exit 1
     fi
   done
}


collect_database_other_db(){
  other_db_bin=$1
  greenplum_username=$2
  greenplum_port=$3
  kae_version=$4
  greenplum_kae_sql=$5
  readelf -a $other_other_db_bin | grep bolt > $log_path/database_other_db.log
  objdump -d $other_other_db_bin | grep crc32cb >> $log_path/database_other_db.log
  if [ "$kae_version" == "1.0" ]; then
    nohup timout 20 watch -gt -n 0.2 cat /sys/class/uacce/hisi_zip*/attrs/available_instances > $log_path/database_greenplum_kae.log &
  else
    nohup timeout 20 watch -gt -n 0.2 cat /sys/class/uacce/hisi_zip*/available_instances > $log_path/database_greenplum_kae.log &
  fi
  psql -h 127.0.0.1 -p $greenplum_port -U $greenplum_username -c "$greenplum_kae_sql"
}


####################机密计算特性信息收集################
collect_virtcca_msg(){
		cvm_name=$1
	username=$2
	passwd=$3
	xml_path=/tmp/temp.xml
	virsh list --all|grep -q $cvm_name
	if [ $? -ne 0 ]; then
    		echo "错误：虚拟机 $cvm_name 不存在"
    		return 0
	fi
	vm_status=$(virsh domstate "$cvm_name")

	if [ "$vm_status" == "shut off" ]; then
    		echo "虚拟机 $cvm_name 处于 shut off 状态，正在启动..."
    		virsh start "$cvm_name"
    		echo "虚拟机 $cvm_name 启动完成"
	elif [ "$vm_status" == "running" ]; then
    		echo "虚拟机 $cvm_name 处于 running 状态，无需操作"
	else
    		echo "错误：无法确定虚拟机 $cvm_name 的状态"
   	        return 0
	fi

	virsh dumpxml $cvm_name > $xml_path
	ret=$(grep -i "type='cvm'" $xml_path)
	echo "$ret" > $log_path/virtcca_status.log
	expect << EOF >> $log_path/virtcca_status.log
        spawn virsh console $cvm_name
        expect "Escape character is \\^]"
        send "\r"
        expect "login:"
        send "$username\r"
        expect "Password:"
        send "$passwd\r"
	expect "# "
	send "ls -l /\r"
	expect "# "
	send "exit\r"
	expect eof
EOF
}


collect_ccos_msg(){
    /vendor/bin/tee-check > $log_path/virtccos_itrustee.log
    tlogcat -f &
    sleep 3s
    cat /var/log/tee/teeOS_log-0  | grep TA_UUID >> $log_path/virtccos_itrustee.log
}


#################加速库特性信息收集##################
collect_acceleration_library(){
    system_lib=$1
    hmpp_lib=$2
    math_lib=$3
    math_jar=$4
    math_java=$5
    openssl speed -elapsed -engine kae rsa2048  > $log_path/acceleration_library.log 2>&1
    ldd $1 >> $log_path/acceleration_library.log
    ldd $2 >> $log_path/acceleration_library.log
    ldd $3 >> $log_path/acceleration_library.log
    jar -tf $4 >> $log_path/acceleration_library_kml_java.log
    javap -v $5 >> $log_path/acceleration_library_kml_java.log
}


###############分布式存储特性信息收集###############
# $1 ec_pool 名字
collect_storage_acc(){
    ec_pool=$1
    ceph_conf=$2
    storage_maintain_bin=$3
    rocksdb_bin=$4
    ucache_bin=$5
    non_ceph_bin=$6
    non_ceph_pid=$7
    # 存储加速库
    ldd /usr/bin/ceph-osd > $log_path/storage_acc.log
    bcache_dev=$(ls /sys/class/block|grep -m 1 bcache)
    # 如果没有课增加异常判断
    ls -l /sys/class/block/$bcache_dev/bcache/cache/internal/traffic_policy_start >> $log_path/storage_acc.log

    pool_list=$(sudo rados lspools |grep -wx $ec_pool)
    if [[ $pool_list =~ $ec_pool ]];
    then
	      echo "ec_pool created" >> $log_path/storage_acc.log
	      pid_num=$(ps -ef|grep  osd|grep -v grep|head -n 1|awk '{print $2}')
	      sudo cat /proc/$pid_num/smaps |grep ksal >> $log_path/storage_acc.log
    else
	      echo "ec_pool not exist" >> $log_path/storage_acc.log
    fi

    systemctl status ceph-boost.service > $log_path/storage_io.log
    if ceph osd pool set vdbench compression_algorithm glz; then
      \cp "$ceph_conf" "$ceph_conf".bak
      sed -i '/^compressor_glz_level/d' "$ceph_conf"
      echo "compressor_glz_level = 1" >> "$ceph_conf"
      systemctl restart ceph.target
      systemctl status ceph.target > $log_path/storage_comporess.log
      \cp "$ceph_conf".bak "$ceph_conf"
    fi
    ldd $storage_maintain_bin > $log_path/storage_maintain_tool.log
    lib_rocksdb=$(ldd $rocksdb_bin | grep librocksdb | awk '{print $3}')
    ldd $lib_rocksdb > $log_path/storage_rocksdb.log
    ldd $ucache_bin > $log_path/storage_ucache.log

    if ldd $non_ceph_bin | grep ksal; then
      timeout 20 perf top -p $non_ceph_pid > $log_path/storage_non_ceph_perf_top.log
    fi
}


###############大数据特性信息收集##################
collect_bigdata_kal(){
  algotithm_list=$1
  algotithm_path=$2
  dataset_list=$3

  read -r -a  algotithm_arry <<< "$algotithm_list"
  read -r -a  dataset_arry <<< "$dataset_list"

  cd $algotithm_path
  index=0
  for per_alg in ${algotithm_list[*]}
  do
      # "Usage: <dataset name> <is raw> <is check>","1st argument: name of dataset: cit_patents, enwiki_2018, uk_2002","2nd argument: optimization algorithm or raw: no/yes","3rd argument: verify result: no/yes"
      if [ "$per_alg" == "betweenness" ] || [ "$per_alg" == "node2vec" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage:<dataset name> <is raw>","1st argument: name of dataset: cit_patents,enwiki_2018,arabic_2005,graph500_22,graph500_23,graph500_25","2nd argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "bfs" ] || [ "$per_alg" == "cc" ] || [ "$per_alg" == "deepwalk" ] || [ "$per_alg" == "diameter" ] || [ "$per_alg" == "ecc" ] || [ "$per_alg" == "fraudar" ] || [ "$per_alg" == "katz" ] || [ "$per_alg" == "kcore" ] || [ "$per_alg" == "ktruss" ] || [ "$per_alg" == "louvain" ] || [ "$per_alg" == "modularity" ] || [ "$per_alg" == "mst" ] || [ "$per_alg" == "scc" ] || [ "$per_alg" == "slpa" ] || [ "$per_alg" == "tpr" ] || [ "$per_alg" == "trussdecomposition" ] || [ "$per_alg" == "wlpa" ] || [ "$per_alg" == "wmssp" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage:<dataset name>","dataset name: simulate1,simulate2,usaRoad"
      if [ "$per_alg" == "cd" ] || [ "$per_alg" == "inccc" ] || [ "$per_alg" == "mce" ] || [ "$per_alg" == "wce" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <weight or not> <isRaw>","1st argument: name of dataset: name of dataset: cit_patents,uk_2002","2nd argument: weight or not: e.g. weighted,unweighted","3rd argument: verify result: no/yes"
      if [ "$per_alg" == "closeness" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} weight no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <weight or not> <isRaw>","1st argument: name of dataset: name of dataset: cit_patents,uk_2002,arabic_2005,graph500_22,graph500_23,graph500_24,graph500_25"
      # "2nd argument: name of api: lcc,avgcc,globalcc","3nd argument: weight or not: weighted,unweighted","4th argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "clusteringcoefficient" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} lcc weighted no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <isRaw>","1st argument: name of dataset: it_2004,twitter7,uk_2007_05,mycielskian20,gap_kron,com_friendster"
      # "2nd argument: name of api: degrees,inDegrees,outDegrees","3rd argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "degree" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} degrees no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <rate> <batch> <isRaw>","1st argument: name of dataset: twitter_2010","2nd argument: rate: e.g. 0.001,0.01,0.05"
      # "3nd argument: batch: e.g. 1,2,3,4,5","4th argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "incpr" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} 0.001 1 no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <queryGraph name> <identical or not>","1st argument: name of dataset: name of dataset: graph500_21,com_orkut"
      # "2nd argument: name of queryGraph: for Identical: 4dgn/4sqr/6star; for unIdentical: 4dgn/4sqr/4clique/5clique/6clique","3rd argument: match mode:Identical,unIdentical"
      if [ "$per_alg" == "incsgm" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} 4dgn Identical > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <isRaw>","1st argument: name of dataset: graph500_22,graph500_24,graph500_25","2nd argument: api: run,runConvergence","3rd argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "lpa" ] || [ "$per_alg" == "pr" ] || [ "$per_alg" == "tc" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} run no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <source number> <isRaw>","1st argument: name of dataset: soc_liveJournal,uk_2002,arabic_2005","2nd argument: source number: 5/50","3rd argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "mssp" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} 5 no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <isRaw>","1st argument: name of dataset: epinions, graph500_23_weight, graph500_25_weight","2nd argument: anomaly_type: 0/1","3rd argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "oddball" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} 0 no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <isRaw> <sourceCnt>","1st argument: name of dataset: cit_patents,uk_2002,arabic_2005","2nd argument: name of api: fixMS,fixSS,conSS"
      # "3rd argument: optimization algorithm or raw: no/yes","4th argument: sourceCnt or null: 1,5,10,50,100"
      if [ "$per_alg" == "ppr" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} fixMS no 1 > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <queryGraph name> <identical or not> <isRaw>","1st argument: name of dataset: name of dataset: graph500_19,liveJournal,com_orkut"
      # "2nd argument: name of queryGraph: for Identical: 4dgn/4sqr/5tree/6star; for unIdentical: 4dgn/4clique/5clique/6clique","3rd argument: match mode:Identical,unIdentical","4th argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "sgm" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} 4dgn Identical no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <seeds count>","1st argument: name of dataset: cit_patents,uk_2002,arabic_2005","2nd argument: name of api: run,runUntilConvergence","3nd argument: seeds count: 100,500,1000"
      if [ "$per_alg" == "tr" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} run 100 > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <isRaw>","1st argument: name of dataset: cage14, GAP_road, GAP_twitter","2nd argument: name of api: static, convergence","3rd argument: optimization algorithm or raw: no, yes"
      if [ "$per_alg" == "wpr" ];
      then
          bash $algotithm_path/bin/graph/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} static no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <data structure> <dataset name> <api name> <isRaw> <isCheck>","1st argument: type of data structure: [dataframe/rdd]","2nd argument: name of dataset: e.g. als/alsbs/alsh"
      # "3rd argument: name of API: e.g. fit/fit1/fit2/fit3; for rdd: train","4th argument: optimization algorithm or raw: [no/yes]","5th argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "als" ] || [ "$per_alg" == "kmeans" ] || [ "$per_alg" == "lda" ] || [ "$per_alg" == "pca" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh dataframe ${dataset_arry[$index]} fit no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <isRaw> <isCheck>","1st argument: name of dataset: e.g. CP10M1K/CP2M5K/CP1M10K","2nd argument: optimization algorithm or raw: [no/yes]","3rd argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "bo" ] || [ "$per_alg" == "cov" ] || [ "$per_alg" == "crf" ] || [ "$per_alg" == "encoder" ] || [ "$per_alg" == "fpg" ] || [ "$per_alg" == "hdb" ] || [ "$per_alg" == "idf" ] || [ "$per_alg" == "if" ] || [ "$per_alg" == "nmf" ] || [ "$per_alg" == "ps" ] || [ "$per_alg" == "simrank" ] || [ "$per_alg" == "svd" ] || [ "$per_alg" == "te" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <isRaw>","1st argument: name of dataset: e.g. bremenSmall/farm/house","2nd argument: optimization algorithm or raw: [no/yes]"
      if [ "$per_alg" == "dbscan" ] || [ "$per_alg" == "knn" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <algorithm type> <data structure> <dataset name> <api name> <isRaw> <ifCheck>","1st argument: type of algorithm: [classification/regression]","2nd argument: type of data structure: [dataframe/rdd]"
      # "3rd argument: name of dataset: [epsilon/higgs/mnist8m]","4th argument: name of API: [for dataframe: fit/fit1/fit2/fit3; for rdd: trainClassifier/trainRegressor]","5th argument: optimization algorithm or raw: [no/yes]"
      # "6th argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "dt" ] || [ "$per_alg" == "gbdt" ] || [ "$per_alg" == "rf" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh classification dataframe ${dataset_arry[$index]} fit no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <save or verify> <isRaw>","1st argument: name of dataset: higgs/mnist8m","2nd argument: name of API: fit/fit1/fit2/fit3"
      # "3rd argument: save or verify result: save/verify","4th argument: optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "dtb" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} fit verify no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <algorithm type> <data structure> <dataset name> <api name> <isRaw> <isCheck>","1st argument: type of algorithm: [classification/regression]"
      # "2nd argument: name of dataset: [higgs/avazu]","3rd argument: name of API: [fit]"
      # "4th argument: optimization algorithm or raw: [no/yes]","5th argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "fm" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh classification ${dataset_arry[$index]} fit no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <algorithm type> <dataset name> <isRaw> <isCheck>","1st argument: type of algorithm: [classification/regression]","2nd argument: name of dataset:mnist8m, higgs "
      # "3rd argument: optimization algorithm or raw: [no/yes]","4th argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "lgbm" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh classification ${dataset_arry[$index]} no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <isRaw> <isCheck>","1st argument: name of dataset: e.g. mnist8m/Twitter/rcv"
      # "2nd argument: name of API: e.g. fit/fit1/fit2/fit3","3th argument: optimization algorithm or raw: [no/yes]","4th argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "linR" ] || [ "$per_alg" == "logR" ] || [ "$per_alg" == "spca" ] || [ "$per_alg" == "svm" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} fit no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <data structure> <dataset name> <isRaw> <isCheck>","1st argument: type of data structure: [dataframe/rdd]","2nd argument: name of dataset: e.g. CP10M1K/CP2M5K/CP1M10K"
      # "3nd argument: optimization algorithm or raw: [no/yes]","4rd argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "pearson" ] || [ "$per_alg" == "spearman" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh dataframe ${dataset_arry[$index]} no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <api name> <isRaw>","1st argument: name of dataset: cate/node/item/taobao","2nd argument: name of API: fit/fit1/fit2/fit3","3rd argument:optimization algorithm or raw: no/yes"
      if [ "$per_alg" == "word2vec" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} fit no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      # "Usage: <dataset name> <algorithm type> <isRaw> <isCheck>","1rd argument: name of dataset: e.g. higgs/mnist8m","2st argument: type of algorithm: [classification/regression]"
      # "3th argument: optimization algorithm or raw: [no/yes]","4th argument: Whether to Compare Results [no/yes]"
      if [ "$per_alg" == "xgbt" ];
      then
          bash $algotithm_path/bin/ml/${algotithm_arry[$index]}_run.sh ${dataset_arry[$index]} classification no no > $log_path/bigdata_kal_${algotithm_arry[$index]}_${dataset_arry[$index]}.log 2>&1
      fi
      index=`expr $index + 1`
  done

}


collect_bigdata_operator(){
        # 日志记录位置 log_path/bigdata_operator.log
	spark_path=$1
	database=$2
    omnioperator_dir=$3
	if [ -e $spark_path ] && [ -e $omnioperator_dir ];
	then
        spark_version=`awk '{print $2}' $spark_path/RELEASE | head -n 1`
        omnioperator_version=`cat $omnioperator_dir/version.txt | grep 'Component Version' | awk -F ": " '{print $2}'`
        echo "spark version:$spark_version omnioperator version:$omnioperator_version" >$log_path/bigdata_operator.log 2>&1
        spark_omni_func=(
        --deploy-mode client
        --driver-cores 5
        --driver-memory 5g
        --num-executors 6
        --executor-cores 2
        --executor-memory 6g
        --master yarn
        --conf spark.memory.offHeap.enabled=true
        --conf spark.memory.offHeap.size=18g
        --conf spark.task.cpus=1
        --conf spark.driver.extraClassPath=${omnioperator_dir}/lib/boostkit-omniop-spark-${spark_version}-${omnioperator_version}-aarch64.jar:${omnioperator_dir}/lib/boostkit-omniop-bindings-${omnioperator_version}-aarch64.jar:${omnioperator_dir}/lib/dependencies/*
        --conf spark.executor.extraClassPath=${omnioperator_dir}/lib/boostkit-omniop-spark-${spark_version}-${omnioperator_version}-aarch64.jar:${omnioperator_dir}/lib/boostkit-omniop-bindings-${omnioperator_version}-aarch64.jar:${omnioperator_dir}/lib/dependencies/*
        --driver-java-options -Djava.library.path=${omnioperator_dir}/lib
        --conf spark.sql.codegen.wholeStage=false
        --conf spark.executorEnv.LD_LIBRARY_PATH=${omnioperator_dir}/lib
        --conf spark.executorEnv.OMNI_HOME=${omnioperator_dir}/
        --conf spark.driverEnv.LD_LIBRARY_PATH=${omnioperator_dir}/lib
        --conf spark.driverEnv.OMNI_HOME=${omnioperator_dir}/
        --conf spark.executor.extraLibraryPath=${omnioperator_dir}/lib
        --conf spark.driverEnv.LD_PRELOAD=${omnioperator_dir}/lib/libjemalloc.so.2
        --conf spark.executorEnv.LD_PRELOAD=${omnioperator_dir}/lib/libjemalloc.so.2
        --conf spark.sql.extensions=com.huawei.boostkit.spark.ColumnarPlugin
        --jars ${omnioperator_dir}/lib/boostkit-omniop-spark-${spark_version}-${omnioperator_version}-aarch64.jar
        --jars ${omnioperator_dir}/lib/boostkit-omniop-bindings-${omnioperator_version}-aarch64.jar
        --conf spark.sql.orc.impl=native
        --conf spark.shuffle.manager=org.apache.spark.shuffle.sort.OmniColumnarShuffleManager
        --conf spark.omni.sql.columnar.fusion=false
        --conf spark.omni.sql.columnar.sortSpill.enabled=true
        --conf spark.omni.sql.columnar.sortSpill.rowThreshold=4000000
        --conf spark.omni.sql.columnar.sortSpill.dirDiskReserveSize=214748364800
        --conf spark.locality.wait=8
        --conf spark.sql.autoBroadcastJoinThreshold=10M
        --conf spark.sql.broadcastTimeout=500
        --conf spark.sql.cbo.enabled=false
        --conf spark.default.parallelism=200
        --conf spark.sql.shuffle.partitions=200
        --conf spark.executorEnv.MALLCO_CONF=narenas:2
)

	    spark_conf_path=$1/conf
        if [ X"${spark_version}" == "X3.1.1" ];then
            if [ ! -f $spark_conf_path/log4j.properties ];then
                cp $spark_conf_path/log4j.properties.template $spark_conf_path/log4j.properties
            fi
            if ! cat < $spark_conf_path/log4j.properties|grep "^log4j.logger.com.huawei.boostkit.spark=INFO";
            then
                    echo "log4j.logger.com.huawei.boostkit.spark=INFO" >> $spark_conf_path/log4j.properties
            fi
        elif [ X"${spark_version}" == "X3.3.1" ];then
            if [ ! -f $spark_conf_path/log4j2.properties ];then
                cp $spark_conf_path/log4j2.properties.template $spark_conf_path/log4j2.properties
            fi
            sed -i "s/^logger.thriftserver.level =.*/logger.thriftserver.level = INFO/" $spark_conf_path/log4j2.properties
        else
            echo "Spark ${spark_version} is not supported." >>$log_path/bigdata_operator.log 2>&1
        fi
	    $spark_path/bin/spark-sql "${spark_omni_func[@]}" --database $database -e "WITH customer_total_return AS ( SELECT sr_customer_sk AS ctr_customer_sk, sr_store_sk AS ctr_store_sk, sum(sr_return_amt) AS ctr_total_return FROM store_returns, date_dim WHERE sr_returned_date_sk = d_date_sk AND d_year = 2000 GROUP BY sr_customer_sk, sr_store_sk) SELECT c_customer_id FROM customer_total_return ctr1, store, customer WHERE ctr1.ctr_total_return > (SELECT avg(ctr_total_return) * 1.2 FROM customer_total_return ctr2 WHERE ctr1.ctr_store_sk = ctr2.ctr_store_sk) AND s_store_sk = ctr1.ctr_store_sk AND s_state = 'TN' AND ctr1.ctr_customer_sk = c_customer_sk ORDER BY c_customer_id LIMIT 100;" 1>>$log_path/bigdata_operator.log 2>&1
	else
	    echo "$spark_path or $omnioperator_dir does not exist" >$log_path/bigdata_operator.log 2>&1
	fi
}


collect_bigdata_hbase(){

   hbase com.huawei.boostkit.hindex.mapreduce.GlobalTableIndexer -Dtablename.to.index=OnlySingleIndexTable -Dindexspecs.to.addandbuild='osindex=>C0:[F1]'
   hbase shell << EOF > $log_path/bigdata_hbase.log 2>&1
   debug
   scan 'OnlySingleIndexTable',{FILTER=>"(SingleColumnValueFilter('C0', 'F1',=,'binary:bbb')"}
   exit
EOF
}


collect_bigdata_tune_up(){
   omniadvisor_dir=$1
   mysql_username=$2
   mysql_password=$3
   mysql_database=$4
   if [ -e $omniadvisor_dir/omniadvisor ];
   then
       echo "omniadvisor.log" >> $log_path/bigdata_tune_up.log
   else
      echo "omniadvisor.log not exist" >> $log_path/bigdata_tune_up.log
   fi

   mysql -u $mysql_username -p$mysql_password -D $mysql_database  -e "show tables" >> $log_path/bigdata_tune_up.log 2>&1

}


collect_bigdata_omnimv(){
  omnimv_dir=$1
  hdfs dfs -ls $omnimv_dir > $log_path/bigdata_omnimv.log
}


collect_bigdata_omni_push_down(){
  omnidata_launcher_server=$1
  omnidata_launcher=$2
  push_down_jars=$3
  push_down_conf=$4
  spark_path=$5
  database=$6
  omnidata_install_path=$7
  zookeeper_address=$8
  zookeeper_path=$9

  if [  -z "$omnidata_launcher_server" ]; then
    sh $omnidata_launcher status > $log_path/bigdata_omni_launcher_status.log
  else
    ssh $omnidata_launcher_server sh $omnidata_launcher status > $log_path/bigdata_omni_launcher_status.log
  fi
  cat << EOF > /tmp/log4j.properties
log4j.rootCategory=INFO, FILE
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.target=System.err
log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p %c{1}: %m%n

log4j.logger.org.apache.spark.sql.execution=DEBUG
log4j.logger.org.apache.spark.repl.Main=INFO

log4j.appender.FILE=org.apache.log4j.FileAppender
log4j.appender.FILE.file=$log_path/bigdata_omni_push_down.log
log4j.appender.FILE.layout=org.apache.log4j.PatternLayout

log4j.appender.FILE.layout.ConversionPattern=%m%n
EOF
  # 这里直接用 operator 的采集 sql
  $spark_path/bin/spark-sql --driver-class-path "$push_down_jars" --jars "$push_down_jars" \
  --conf "$push_down_conf" \
  --conf spark.sql.cbo.enabled=true \
  --conf spark.sql.cbo.planStats.enabled=true \
  --conf spark.sql.ndp.enabled=true \
  --conf spark.sql.ndp.filter.selectivity.enable=true \
  --conf spark.sql.ndp.filter.selectivity=0.5 \
  --conf spark.sql.ndp.alive.omnidata=3 \
  --conf spark.sql.ndp.table.size.threshold=10 \
  --conf spark.sql.ndp.zookeeper.address=$zookeeper_address \
  --conf spark.sql.ndp.zookeeper.path=$zookeeper_path \
  --conf spark.sql.ndp.zookeeper.timeout=15000 \
  --conf spark.driver.extraLibraryPath=$omnidata_install_path/haf-host/lib \
  --conf spark.executor.extraLibraryPath=$omnidata_install_path/haf-host/lib \
  --conf spark.executorEnv.HAF_CONFIG_PATH=$omnidata_install_path/haf-host/etc/ \
  --name tpcds_test.sql --driver-memory 50G --driver-java-options -Dlog4j.configuration=file:/tmp/log4j.properties \
  --executor-memory 32G --num-executors 30 --executor-cores 18 --database tpcds_bin_partitioned_varchar_orc_2 \
  -e "WITH customer_total_return AS ( SELECT sr_customer_sk AS ctr_customer_sk, sr_store_sk AS ctr_store_sk, sum(sr_return_amt) AS ctr_total_return FROM store_returns, date_dim WHERE sr_returned_date_sk = d_date_sk AND d_year = 2000 GROUP BY sr_customer_sk, sr_store_sk) SELECT c_customer_id FROM customer_total_return ctr1, store, customer WHERE ctr1.ctr_total_return > (SELECT avg(ctr_total_return) * 1.2 FROM customer_total_return ctr2 WHERE ctr1.ctr_store_sk = ctr2.ctr_store_sk) AND s_store_sk = ctr1.ctr_store_sk AND s_state = 'TN' AND ctr1.ctr_customer_sk = c_customer_sk ORDER BY c_customer_id LIMIT 100;"
  rm -f /tmp/log4j.properties
}


collect_bigdata_omni_shuffle(){
  spark_path=$1
  shuffle_jars=$2
  database=$3
  cat << EOF > /tmp/ock_spark.conf
spark.master yarn
spark.task.cpus 1
spark.shuffle.compress true
EOF
  timeout 20 $spark_path/bin/spark-sql --deploy-mode client --driver-cores 8 --driver-memory 40G --num-executors 24 --executor-cores 12 --executor-memory 25g --master yarn --conf spark.sql.codegen.wholeStage=false --jars $shuffle_jars --properties-file /tmp/ock_spark.conf --database $database > $log_path/bigdata_omni_shuffle.log 2>&1
  rm -f /tmp/ock_spark.conf
}


collect_bigdata_components(){
  spark_path=$1
  $spark_path/bin/spark-sql --version && echo spark > $log_path/bigdata_components.log
  hive --version && echo hive >> $log_path/bigdata_components.log
  hbase version && echo hbase >> $log_path/bigdata_components.log
}


#################HPC特性信息收集##################
# $1 #用户可执行文件路径

collect_hpc_acceleration_library(){
    bin_path=$1
    rm -rf $log_path/hpc_acceleration_library.log
    touch $log_path/hpc_acceleration_library.log
    ldd $bin_path > $log_path/hpc_acceleration_library.log  2>&1
}

# $1 #用户可执行文件路径
collect_sme_acceleration_library(){
    bin_path=$1
    ifsme=`lscpu|grep Flags|grep sme`
    if [ -n "$ifsme" ]; then
        bin_path=$1 #用户可执行文件路径
        rm -rf $log_path/hpc_SME_library.log
        touch $log_path/hpc_SME_library.log
        ldd $bin_path | grep SME >> $log_path/hpc_SME_library.log 2>&1
        objdump -d $bin_path >> $log_path/hpc_SME_library.log 2>&1
    else
      echo "架构不支持SME" >> $log_path/hpc_SME_library.log
    fi

}

collect_sve_source_code(){
  grep -r arm_sve.h "$1" > $log_path/hpc_sve.log
}


################环境信息收集#######################
collect_os_msg(){
    echo  os: $(cat /etc/os-release |grep PRETTY_NAME=|awk -F= '{print $2}') >  $log_path/os_msg.log
    echo kernel: $(uname -r) >> $log_path/os_msg.log
    dmidecode -t Processor|grep -m 1 Version: >> $log_path/os_msg.log
    dmidecode -t system|grep "Product Name" >> $log_path/os_msg.log
  }


main(){
  select_project=$(acquire_select_project)
  echo "开始收集BoostKit 特性信息如下：$select_project"
  mkdir -p $log_path
  rm -fr $log_path/*
  echo "日志存放位置： $log_path"

  collect_os_msg
	for per_project in $select_project
	do
	    if [ $per_project = "Arm" ];
		  then
		    	kbox_container=$(acquire_value Arm kbox_container)
	        video_container=$(acquire_value Arm video_container)
	        instruction_container=$(acquire_value Arm instuction_container)
	        echo "start collect Arm msg..."
	        collect_arm_native $kbox_container $video_container $instruction_container
	        echo "Arm collect msg Done..."
	    elif [ $per_project = "Virt" ];
		  then
			    echo "start collect Virt msg..."
			    cvm_name=$(acquire_value Virtcca cvm_name)
          cvm_username=$(acquire_value Virtcca cvm_username)
          cvm_password=$(acquire_value Virtcca cvm_password)
          collect_ccos_msg
          collect_virtcca_msg $cvm_name $cvm_username $cvm_password
          echo "Virt collect msg Done..."
		  elif [ $per_project = "Database" ];
		  then
		      echo "start collect Database msg..."
		      use_mysql=$(acquire_value Database use_mysql)
		      mysql_install_path=$(acquire_value Database mysql_install_path)
		      mysql_username=$(acquire_value Database mysql_username)
		      mysql_password=$(acquire_value Database mysql_password)
		      mysql_port=$(acquire_value Database mysql_port)
		      database_name=$(acquire_value Database database_name)
		      nvme_name=$(acquire_value Database nvme_name)

          if [ "$use_mysql" == "1" ]; then
            collect_database  $mysql_install_path $mysql_username $mysql_password $mysql_port $database_name $nvme_name
            echo "Database mysql collect msg Done..."
          else
            other_db_bin=$(acquire_value Database other_db_bin)
            greenplum_username=$(acquire_value Database greenplum_username)
            greenplum_port=$(acquire_value Database greenplum_port)
            kae_version=$(acquire_value Database kae_version)
            greenplum_kae_sql=$(acquire_value Database greenplum_kae_sql)
            collect_database_other_db "$other_db_bin" "$greenplum_username" "$greenplum_password" "$greenplum_port" "$kae_version" "$greenplum_kae_sql"
            echo "Database other db collect msg Done..."
          fi
      elif [ $per_project = "Acclib" ];
      then
           echo "start collect acceleration_library msg..."
           system_lib=$(acquire_value Acclib system_lib)
           hmpp_lib=$(acquire_value Acclib HMPP_lib)
           math_lib=$(acquire_value Acclib math_lib)
           math_jar=$(acquire_value Acclib math_jar)
           math_java=$(acquire_value Acclib math_java)
           collect_acceleration_library "$system_lib" "$hmpp_lib" "$math_lib" "$math_jar" "$math_java"
           echo "acceleration_library collect msg Done..."
      elif [ $per_project = "Storage" ];
      then
          echo "start collect Storage msg..."
          ec_pool_name=$(acquire_value Storage ec_pool_name)
          ceph_conf=$(acquire_value Storage ceph_conf)
          storage_maintain_bin=$(acquire_value Storage storage_maintain_bin)
          rocksdb_bin=$(acquire_value Storage rocksdb_bin)
          ucache_bin=$(acquire_value Storage ucache_bin)
          non_ceph_bin=$(acquire_value Storage non_ceph_bin)
          non_ceph_pid=$(acquire_value Storage non_ceph_pid)
          collect_storage_acc "$ec_pool_name" "$ceph_conf" "$storage_maintain_bin" "$rocksdb_bin" "$ucache_bin" "$non_ceph_bin" "$non_ceph_pid"
          echo "Storage collect msg Done..."
      elif [ $per_project = "Bigdata" ];
      then
          echo "start collect Bigdata msg..."
          algorithms_path=$(acquire_value Bigdata algorithms_path)
          algorithms_list=$(acquire_value Bigdata algorithms_name)
          dataset_list=$(acquire_value Bigdata dataset_list)
          spark_path=$(acquire_value Bigdata spark_path)
          database=$(acquire_value Bigdata database)
          omnioperator_dir=$(acquire_value Bigdata omnioperator_dir)
          omniadvisor_dir=$(acquire_value Bigdata omniadvisor_dir)
          mysql_username=$(acquire_value Bigdata mysql_username)
          mysql_password=$(acquire_value Bigdata mysql_password)
          mysql_database_name=$(acquire_value Bigdata mysql_database_name)
          omnimv_dir=$(acquire_value Bigdata omnimv_dir)
          omnidata_launcher=$(acquire_value Bigdata omnidata_launcher)
          omnidata_launcher_server=$(acquire_value Bigdata omnidata_launcher_server)
          omnidata_install_path=$(acquire_value Bigdata omnidata_install_path)
          push_down_jars=$(acquire_value Bigdata push_down_jars)
          push_down_conf=$(acquire_value Bigdata push_down_conf)
          zookeeper_address=$(acquire_value Bigdata zookeeper_address)
          zookeeper_path=$(acquire_value Bigdata zookeeper_path)
          shuffle_jars=$(acquire_value Bigdata shuffle_jars)
          collect_bigdata_components $spark_path
          collect_bigdata_kal "${algorithms_list[@]}" $algorithms_path "${dataset_list[@]}"
          collect_bigdata_operator $spark_path $database $omnioperator_dir
          collect_bigdata_hbase
          collect_bigdata_tune_up $omniadvisor_dir $mysql_username $mysql_password $mysql_database_name
          collect_bigdata_omnimv "$omnimv_dir"
          collect_bigdata_omni_push_down "$omnidata_launcher_server" "$omnidata_launcher" "$push_down_jars" "$push_down_conf" "$spark_path" "$database" "$omnidata_install_path" "$zookeeper_address" "$zookeeper_path"
          collect_bigdata_omni_shuffle "$spark_path" "$shuffle_jars" "$database"
          echo "Bigdata collect msg Done..."
      elif [ $per_project = "Virtual" ];
      then
        echo "start collect Virtual msg..."
        collect_virtual_host
        server_name=$(acquire_value Virtual server_name)
        network=$(acquire_value Virtual network)
        flavor=$(acquire_value Virtual flavor)
        volume=$(acquire_value Virtual volume)
        availability_zone=$(acquire_value Virtual availability_zone)
        collect_virtual_dpu $server_name $network $flavor $volume $availability_zone
        vm_ip=$(acquire_value Virtual vm_ip)
        vm_user=$(acquire_value Virtual vm_user)
        vm_password=$(acquire_value Virtual vm_password)
        vm_ceph_disk_name=$(acquire_value Virtual vm_ceph_disk_name)
        vm_name=$(acquire_value Virtual vm_name)
        collect_virtual_ceph "$vm_ip" "$vm_user" "$vm_password" "$vm_ceph_disk_name" "$vm_name"
        collect_virtual_ovs_xpf
        echo "Virtual collect msg Done..."
      elif [ $per_project = "HPC" ];
      then
        echo "start collect HPC msg..."
        acc_lib=$(acquire_value HPC acc_lib)
        sme=$(acquire_value HPC sme)
        sve_source_code=$(acquire_value HPC sve_source_code)
        collect_hpc_acceleration_library $acc_lib
        collect_sme_acceleration_library $sme
        collect_sve_source_code $sve_source_code
        echo "HPC collect msg Done..."
		fi
    done
}

if [ -e $config_file ]; then
  customer_information=$(acquire_value Global information)
  check_customer_info $customer_information
else
  echo "config.ini not exist"
  exit 1
fi
main
tar_log_file $customer_information













