from pipeline_script_generator.script_generator import ScriptGenerator


class GitlabScript(ScriptGenerator):
    name = "gitlab"
    seprator = "#"
    base_template = """
stages:
  - migrating-applications
  - affinity-analysis  
  - build
  - test
  - clamav

variables:
  # 源码迁移参数
  # 源码构建命令，在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住
  SOURCE_CODE_COMMAND: "make"
  
  # 软件迁移评估参数
  # 被扫描的软件包的路径
  SOFTWARE_PATH: ""
  
  # 字节对齐检查参数
  # 源码构建命令，在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住
  BYTE_ALIGNMENT_COMMAND: "make"
  # 构建工具，当前工具支持make、cmake和automake，默认选项为make
  BYTE_ALIGNMENT_TOOL: "make"
  
  # 内存一致性检查参数
  # BC文件路径
  MEMORY_BC_FILE: ""
  
  # 向量化检查参数
  # BC文件路径
  VECTORIZED_BC_FILE: ""
  # 源码构建命令，在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住
  VECTORIZED_COMMAND: "make"
  
  # 编译参数
  # 编译命令
  BUILD_COMMAND: ""
  # A-FOT配置文件存放路径
  A_FOT_CONF_PATH: ""
  
  # Java Performance Analysis功能参数
  # 需要采集的目标程序所在的服务器地址， 多个使用逗号隔离
  JAVA_TARGET_SERVER_IP: ""
  # 需要采集的应用名称，多个采用逗号隔离
  JAVA_APPLICATION_NAME: ""
  # 采集目标应用时间，单位秒。当存在-j参数时，jmeter结束或者到达采集执行时间，结束采集
  JAVA_COLLECTION_APPLICATION_DURATION: ""
  # jmeter执行命令。例如：bash /opt/apache-jmeter-5.6.3/bin/jmeter.sh -nt /home/xxx/Request.jmx -l /home/xxx/result.html -eo /home/xxx/report
  JAVA_JMETER_COMMAND: ""
  # jmeter -l参数，将测试结果以指定的文件名保存到本地
  JAVA_JMETER_RESULT_FILE_NAME: ""
  # jmeter -o参数，jmeter运行结束后生成的测试报告文件路径
  JAVA_JMETER_RESULT_REPORT_DIR: ""
  # Devkit工具部署的环境IP
  DEVKIT_WEB_IP: ""
  
##STAGES##
"""
    source_migration_template = """
# 源码迁移
source-code-migration:
  stage: migrating-applications
  tags:     
    - kunpeng_scanner # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 源码迁移 ======'
    - devkit porting src-mig -i ./ -c $SOURCE_CODE_COMMAND -r html || [ $? -eq 1 ] && echo 'Warning:扫描报告包含建议项'
    - mv ./src-mig*.html ./SourceCodeScanningReport.html
  artifacts:
    paths:
      - SourceCodeScanningReport.html 
    name: src-mig
"""
    package_migration_template = """
# 软件迁移评估
software-migration-assessment:
  stage: migrating-applications
  tags:
    - kunpeng_scanner # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 软件迁移评估 ======'
    - ls /usr/local/bin/devkit
    - devkit porting pkg-mig -i $SOFTWARE_PATH -r html || [ $? -eq 1 ] && echo 'Warning:扫描报告包含建议项'
    - mv ./pkg-mig*.html ./SoftwareMigrationAssessment.html
  artifacts:
    paths:
      - SoftwareMigrationAssessment.html     
    name: pkg-mig    
"""
    mode_check_template = """
# 64位运行模式检查
64-bit-running-mode-check:
  stage: affinity-analysis
  tags:
    - kunpeng_scanner # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 64位运行模式检查 ======'
    - devkit advisor mode-check -i ./ -r html
    - mv ./mode_check*.html ./64-bit-running-mode-check.html
  artifacts:
    paths:
      - 64-bit-running-mode-check.html      
    name: mode-check    
"""
    byte_alignment_template = """
# 字节对齐检查
byte-alignment-check:
  stage: affinity-analysis
  tags:
    - kunpeng_scanner # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 字节对齐检查 ======'
    - devkit advisor byte-align -i ./ -c $BYTE_ALIGNMENT_COMMAND -b $BYTE_ALIGNMENT_TOOL -r html
    - mv ./byte-align*.html ./byte-alignment-check.html
  artifacts:
    paths:
      - byte-alignment-check.html
    name: byte-align
"""
    memory_consistency_template = """
# 内存一致性检查
memory-consistency-check:
  stage: affinity-analysis
  tags:
    - kunpeng_scanner  # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 内存一致性检查 ======'
    # 需编写生成的BC文件脚本
    - devkit advisor mem-cons -i ./ -f $MEMORY_BC_FILE -r html
    - mv ./mem-cons*.html ./memory-consistency-check.html
  artifacts:
    paths:
      - memory-consistency-check.html     
    name: mem-cons
"""
    vector_check_template = """
# 向量化检查
vectorized-check:
  stage: affinity-analysis
  tags:
    - kunpeng_scanner # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 向量化检查 ======'
    # 需编写生成的BC文件脚本;
    - devkit advisor vec-check -i ./ -f $VECTORIZED_BC_FILE -c $VECTORIZED_COMMAND -r html
    - mv ./vec-check*.html ./vectorized-check.html
  artifacts:
    paths:
      - vectorized-check.html
    name: vec-check
"""
    gcc_template = """
# 普通编译
build:
  stage: build
  tags:
    - kunpeng_c_builder_gcc # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - $BUILD_COMMAND
"""
    bisheng_compiler_template = """
# 使用毕昇编译器编译
build:
  stage: build
  tags:
    - kunpeng_c_builder_bisheng_compiler # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - $BUILD_COMMAND
"""
    java8_build_template = """
# 普通编译
build:
  stage: build
  tags:
    - kunpeng_java_builder_jdk8 # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - $BUILD_COMMAND    
"""
    java17_build_template = """
# 普通编译
build:
  stage: build
  tags:
    - kunpeng_java_builder_jdk17 # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - $BUILD_COMMAND    
"""
    a_fot_template = """
# 使用GCC for openEuler编译，使用A-FOT工具时，需要根据用户指南填写配置项
A-FOT:
  stage: build
  tags:
    - kunpeng_c_builder_gcc # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - export PATH=${HOME}/.local/gcc-10.3.1-2023.12-aarch64-linux/bin:$PATH
    - a-fot --config_file $A_FOT_CONF_PATH/a-fot.ini    
"""
    java_perf_template = """
# java性能调优
java-performance-analysis:
  stage: test
  tags:
    - kunpeng_tester # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - /usr/bin/rm -rf ./report_dir/*.html
    - set -e 
    - CURDIR=$(pwd) 
    # 删除上次jmeter产生的报告 (jmeter 命令-l、-o指定的文件和路径) 
    - rm -rf ${JAVA_JMETER_RESULT_FILE_NAME} ${JAVA_JMETER_RESULT_REPORT_DIR}
    # 运行java性能采集 
    - ${HOME}/.local/devkit_tester/bin/entrance -i ${TARGET_SERVER_IP} -u root -f ${HOME}/.ssh/id_rsa -D ${DEVKIT_WEB_IP} -a ${JAVA_APPLICATION_NAME} -d ${JAVA_COLLECTION_APPLICATION_DURATION} -g ./${GIT_TARGET_DIR_NAME} -j 'sh ${JAVA_JMETER_COMMAND}' -o ./
  artifacts:
    paths:
      - devkit_performance_report.html
    name: Java_Performance_Report
"""
    compatibility_test_template = """
# 鲲鹏兼容测试  
compatibility_test:       # This job runs in the build stage, which runs first.
  stage: test
  tags:
    - kunpeng_executor # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - CURDIR=$(pwd)
    - echo $CURDIR
    - cp -rf ${HOME}/.local/compatibility_testing/template.html.bak ${HOME}/.local/compatibility_testing/template.html
    - ${HOME}/.local/compatibility_testing/bin/compatibility_test 
    - cp -rf ${HOME}/.local/compatibility_testing/compatibility_report.html $CURDIR/compatibility_report.html
    - sudo /bin/bash ${HOME}/.local/compatibility_testing/report_result.sh
    - echo "请去 '${CURDIR}'/compatibility_report.html 查看报告 "
  artifacts:
    paths:
      - compatibility_report.html     
    name: compatibility_report
"""
    clamav_template = """
# 病毒扫描
clamscan:
  stage: clamav
  tags:
    - kunpeng_clamav # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - freshclam
    - clamscan -i -r ./ -l ./clamscan.log
  artifacts:
    paths:
      - clamscan.log
    name: clamscan    
"""