#!/bin/bash
current_path=$(pwd)
config_file=$current_path/config.ini
log_path=$current_path/log
default_project="Bigdata Database Storage Arm Virt Acclib Virtual HPC"

spark_omni_func=(
    --deploy-mode client
    --driver-cores 1
    --driver-memory 980M
    --num-executors 3
    --executor-cores 1
    --executor-memory 600M
    --master yarn
    --conf spark.memory.offHeap.enabled=true
    --conf spark.memory.offHeap.size=1025M
    --conf spark.task.cpus=1
    --conf spark.driver.extraClassPath=/opt/omni-operator/lib/boostkit-omniop-spark-3.1.1-1.4.0-aarch64.jar:/opt/omni-operator/lib/boostkit-omniop-bindings-1.4.0-aarch64.jar:/opt/omni-operator/lib/dependencies/*
    --conf spark.executor.extraClassPath=/opt/omni-operator/lib/boostkit-omniop-spark-3.1.1-1.4.0-aarch64.jar:/opt/omni-operator/lib/boostkit-omniop-bindings-1.4.0-aarch64.jar:/opt/omni-operator/lib/dependencies/*
    --driver-java-options -Djava.library.path=/opt/omni-operator/lib
    --conf spark.sql.codegen.wholeStage=false
    --conf spark.executorEnv.LD_LIBRARY_PATH=/opt/omni-operator/lib
    --conf spark.executorEnv.OMNI_HOME=/opt/omni-operator/
    --conf spark.driverEnv.LD_LIBRARY_PATH=/opt/omni-operator/lib
    --conf spark.driverEnv.OMNI_HOME=/opt/omni-operator/
    --conf spark.executor.extraLibraryPath=/opt/omni-operator/lib
    --conf spark.driverEnv.LD_PRELOAD=/opt/omni-operator/lib/libjemalloc.so.2
    --conf spark.executorEnv.LD_PRELOAD=/opt/omni-operator/lib/libjemalloc.so.2
    --conf spark.sql.extensions=com.huawei.boostkit.spark.ColumnarPlugin
    --jars /opt/omni-operator/lib/boostkit-omniop-spark-3.1.1-1.4.0-aarch64.jar
    --jars /opt/omni-operator/lib/boostkit-omniop-bindings-1.4.0-aarch64.jar
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



#####################根据section以及key值获取对应value############
acquire_value(){
    project=$1
    key=$2
    grep $project -A 15  $config_file |grep -m 1 $key|awk -F= '{print $2}'|awk '{print $1}'
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


################数据库特性信息收集##################
collect_database(){
   mysql_install_path=$1
   mysql_username=$2
   mysql_password=$3
   database_name=$4
   plugin_path=$1/lib/plugin
   $mysql_install_path/bin/mysqld_safe --defaults-file=/etc/my.cnf &
   sleep 20
   mysql -u $mysql_username -p$mysql_password -D $database_name -e "select * from INFORMATION_SCHEMA.plugins where PLUGIN_NAME like 'thread_pool%'" > $log_path/database_mysql.log
   mysql -u $mysql_username -p$mysql_password -D $database_name -e "select * from INFORMATION_SCHEMA.plugins where PLUGIN_NAME like 'kovae%'" >> $log_path/database_mysql.log
   echo thread_pool: $(ls $plugin_path |grep thread_pool.so) >> $log_path/database_mysql.log
   echo kovae_path: $(ls $plugin_path |grep ha_kovae.so) >>  $log_path/database_mysql.log
   readelf -a $mysql_install_path/bin/mysqld|grep bolt >> $log_path/database_mysql.log
   echo no_lock: $(objdump -d $mysql_install_path/bin/mysqld|grep -c row_vers_build_for_semi_consistent_readP5trx_t) >> $log_path/database_mysql.log
   objdump -d $mysql_install_path/bin/mysqld |grep crc32cb >> $log_path/database_mysql.log
   pkill -9 mysql
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
    openssl speed -elapsed -engine kae rsa2048  > $log_path/acceleration_library.log 2>&1
    ldd $1 >> $log_path/acceleration_library.log
    ldd $2 >> $log_path/acceleration_library.log
    ldd $3 >> $log_path/acceleration_library.log
}


###############分布式存储特性信息收集###############
# $1 ec_pool 名字
collect_storage_acc(){
    ec_pool=$1
    # 存储加速库
    ldd /usr/bin/ceph-osd > $log_path/storage_acc.log
    bcache_dev=$(ls /sys/class/block|grep -m 1 bcache)
    # 如果没有课增加异常判断
    ll /sys/class/block/$bcache_dev/bcache/cache/internal/traffic_policy_start >> $log_path/storage_acc.log

    pool_list=$(rados lspools |grep -wx $ec_pool)
    if [[ $pool_list =~ $ec_pool ]];
    then
	      echo "ec_pool created" >> $log_path/storage_acc.log
	      pid_num=$(ps -ef|grep  osd|grep -v grep|head -n 1|awk '{print $2}')
	      cat /proc/$pid_num/smaps |grep ksal >> $log_path/storage_acc.log
    else
	      echo "ec_pool not exist" >> $log_path/storage_acc.log
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
	if [ -e $spark_path ];
	then
	    spark_conf_path=$1/conf
	    if ! cat < $spark_conf_path/log4j.properties|grep "^log4j.logger.com.huawei.boostkit.spark=INFO";
	    then
                echo "log4j.logger.com.huawei.boostkit.spark=INFO" >> $spark_conf_path/log4j.properties
	    fi
	    $spark_path/bin/spark-sql "${spark_omni_func[@]}" --database $database -e "WITH customer_total_return AS ( SELECT sr_customer_sk AS ctr_customer_sk, sr_store_sk AS ctr_store_sk, sum(sr_return_amt) AS ctr_total_return FROM store_returns, date_dim WHERE sr_returned_date_sk = d_date_sk AND d_year = 2000 GROUP BY sr_customer_sk, sr_store_sk) SELECT c_customer_id FROM customer_total_return ctr1, store, customer WHERE ctr1.ctr_total_return > (SELECT avg(ctr_total_return) * 1.2 FROM customer_total_return ctr2 WHERE ctr1.ctr_store_sk = ctr2.ctr_store_sk) AND s_store_sk = ctr1.ctr_store_sk AND s_state = 'TN' AND ctr1.ctr_customer_sk = c_customer_sk ORDER BY c_customer_id LIMIT 100;" 1>$log_path/bigdata_operator.log 2>&1
	else
	    echo "$spark_path not exist" >$log_path/bigdata_operator.log 2>&1
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
		      mysql_install_path=$(acquire_value Database mysql_install_path)
		      mysql_username=$(acquire_value Database mysql_username)
		      mysql_password=$(acquire_value Database mysql_password)
		      database_name=$(acquire_value Database database_name)

          collect_database  $mysql_install_path $mysql_username $mysql_password $database_name
          echo "Database collect msg Done..."
      elif [ $per_project = "Acclib" ];
      then
           echo "start collect acceleration_library msg..."
           system_lib=$(acquire_value Acclib system_lib)
           hmpp_lib=$(acquire_value Acclib HMPP_lib)
           math_lib=$(acquire_value Acclib math_lib)
           collect_acceleration_library $system_lib $hmpp_lib $math_lib
           echo "acceleration_library collect msg Done..."
      elif [ $per_project = "Storage" ];
      then
          echo "start collect Storage msg..."
          ec_pool_name=$(acquire_value Storage ec_pool_name)
          collect_storage_acc ec_pool_name
          echo "Storage collect msg Done..."
      elif [ $per_project = "Bigdata" ];
      then
          echo "start collect Bigdata msg..."
          algorithms_path=$(acquire_value Bigdata algorithms_path)
          algorithms_list=$(acquire_value Bigdata algorithms_name)
          dataset_list=$(acquire_value Bigdata dataset_list)
          spark_path=$(acquire_value Bigdata spark_path)
          database=$(acquire_value Bigdata database)
          omniadvisor_dir=$(acquire_value Bigdata omniadvisor_dir)
          mysql_username=$(acquire_value Bigdata mysql_username)
          mysql_password=$(acquire_value Bigdata mysql_password)
          mysql_database_name=$(acquire_value Bigdata mysql_database_name)
          collect_bigdata_kal "${algorithms_list[@]}" $algorithms_path "${dataset_list[@]}"
          collect_bigdata_operator $spark_path $database
          collect_bigdata_hbase
          collect_bigdata_tune_up $omniadvisor_dir $mysql_username $mysql_password $mysql_database_name
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
        echo "Virtual collect msg Done..."
      elif [ $per_project = "HPC" ];
      then
        echo "start collect HPC msg..."
        acc_lib=$(acquire_value HPC acc_lib)
        sme=$(acquire_value HPC sme)
        collect_hpc_acceleration_library $acc_lib
        collect_sme_acceleration_library $sme
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









