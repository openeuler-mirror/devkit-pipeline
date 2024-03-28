<center><big><b>《gitlab流水线迁移、亲和命令行配置》</b></big></center>

##### 命令行状态码含义
| 状态码  |  触发情况 |
| ------------ | ------------ |
| 0 |  无扫描建议 |
| 1 |  扫描结果只存在建议项 |
| 2 |  扫描任务出现Ctrl+C(SIGINT(2)) |
| 3 |  扫描结果超时 |
| 4 |  扫描命令错误 |
| 5 |  扫描结果存在必须修改项 |
| 15/20 | 扫描任务出现Ctrl+Z SIGTERM(15)/SIGTSTP(20) |

 **注：命令行中 -i ./ 时为扫描本仓库代码** 
##### 软件迁移评估：

```
stages:  
  - build    
  - migrating-applications

software-migration-assessment:
  stage: migrating-applications
  tags:
    - kunpeng_c_cpp_builder # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 软件迁移评估 ======'
    - devkit porting pkg-mig -i 待扫描软件包 -r html || [ $? -eq 1 ] && echo 'Warning:扫描报告包含建议项'
    
    # 示例 devkit porting pkg-mig -i impala-2.9.0+cdh5.12.1+0-1.cdh5.12.1.p0.3.el7.x86_64.rpm -r html || [ $? -eq 1 ] && echo 'Warning:扫描报告包含建议项'
    - mv ./pkg-mig*.html ./SoftwareMigrationAssessment.html

  artifacts:
    paths:
      - SoftwareMigrationAssessment.html
    name: pkg-mig
    

```
具体参数如下

| 参数  | 参数选项  | 参数说明  |
| ------------ | ------------ | ------------ |
|  -i/--input | package_path  | 必选参数。待扫描的软件包路径，若存在多个扫描路径需使用英文逗号分割。例如：/home/test1.jar, /home/test2.jar。  |
| -t/--target-os  | target-os  |  可选参数。待扫描的目标操作系统。|
| -o/--output  |  output_path |  可选参数。报告存放路径。报告默认存放在当前执行路径下，名称默认为“特性名称_时间戳”。 |
|  --set-timeout | time  | 可选参数。任务超时时间。默认无超时时间，任务将持续执行直到结束。 |
|  -l/--log-level | 0,1,2,3  |  可选参数。任务日志级别。0（DEBUG）、1（INFO）、2（WARNING）、3（ERROR），默认为1（INFO）。 |
|  -r/--report-type | all,json,html,csv |  可选参数。扫描报告的格式。默认为all，即默认生成json、html、csv三种报告 |

##### 源码迁移：

```
stages:  
  - build    
  - migrating-applications

source-code-migration:
  stage: migrating-applications
  tags:
    - kunpeng_c_cpp_builder # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 源码迁移 ======'
    - devkit porting src-mig -i 待扫描源码的文件夹或压缩包路径 -c 源码的构建命令 -r html || [ $? -eq 1 ] && echo 'Warning:扫描报告包含建议项'
    
    # 示例 devkit porting src-mig -i wtdbg2-2.5 -c make -r html || [ $? -eq 1 ] && echo 'Warning:扫描报告包含建议项'
    - mv ./src-mig*.html ./SourceCodeScanningReport.html

  artifacts:
    paths:
      - SourceCodeScanningReport.html
    name: src-mig

```
具体参数如下
| 参数  |  参数选项 | 参数说明  |
| ------------ | ------------ | ------------ |
|  -i/--input-path |  path | 必选参数。待扫描源码的文件夹或压缩包路径，若存在多个扫描路径需使用英文逗号分割。例如：/home/test1,/home/test2。  |
| -c/--cmd  |  cmd |  必选参数。源码的构建命令。例如：make all。 |
| -s/--source-type  | c,c++,asm,fortran,go, interpreted  |  可选参数。待扫描源码类型。 |
|  -t/--target-os |  target-os | 可选参数。迁移的目标操作系统。如果用户不输入则默认为当前操作系统。例如：bclinux7.7。  |
| -p/--compiler  |  gcc,clang | 可选参数。编译器版本。默认为选定目标操作系统的默认GCC版本。例如：gcc7.8.5  |
| -f/--fortran-compiler  |  flang-ver |  可选参数。fortran代码的编译器版本。默认为flang2.5.0.1。 |
|  -b/--build-tool | make,cmake,automake,go  | 可选参数。构建工具。默认make，只有在--source-type中没有c/c++/asm/fortran并且包含go时可以选go。  |
| -o/--output  | /home/test/report  |  可选参数。扫描报告的存放地址和文件名称。默认存放在当前执行路径下，名称默认为功能名_时间戳_uuid(4位)。 |
|  --set-timeout | time  | 可选参数。命令行的超时时间。默认无超时时间。  |
|  -l/--log-level |  0,1,2,3 | 可选参数。设置日志级别。0（DEBUG）、1（INFO）、2（WARNING）、3（ERROR），默认为1（INFO）。  |
| -r/--report-type  | all,json,html,csv  | 可选参数。 扫描报告的格式。默认为all，选择all的时候json、csv和html报告都会生成。 |
|  --ignore |  /opt/ignore.json |  可选参数。屏蔽扫描规则信息。 |
##### 64位运行模式检查：
```
stages:  
  - build    
  - affinity-analysis

64-bit-running-mode-check:
  stage: affinity-analysis
  tags:
    - kunpeng_c_cpp_builder # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 64位运行模式检查 ======'
    - devkit advisor mode-check -i 待扫描的软件包路径 -r html
    
    # 示例 devkit advisor mode-check -i /opt/DevKit/testcase/affinity/precheck/test005 -r html
    - mv ./mode_check*.html ./64-bit-running-mode-check.html

  artifacts:
    paths:
      - 64-bit-running-mode-check.html
    name: mode-check


```
具体参数如下
|  参数 |  参数选项 |  参数说明 |
| ------------ | ------------ | ------------ |
| -i/--input  |  package_path |  必选参数。待扫描的源码文件夹路径，若存在多个扫描路径需使用英文逗号分割。例如：/home/test1, /home/test2。 |
| -o/--output  |  output_path |  可选参数。报告存放路径。报告默认存放在当前执行路径下，名称默认为“特性名称_时间戳”。 |
| --set-timeout | time  |  可选参数。任务超时时间。默认无超时时间，任务将持续执行直到结束。 |
| -l/--log-level  |  0,1,2,3 | 可选参数。日志等级，0（DEBUG）、1（INFO）、2（WARNING）、3（ERROR），默认为1（INFO）。  |
| -r/--report-type  |  all,json,html,csv | 可选参数。扫描报告的格式。默认为all，即默认生成json、html、csv三种报告。  |

##### 字节对齐检查：
```
stages:  
  - build    
  - affinity-analysis

byte-alignment-check:
  stage: affinity-analysis
  tags:
    - kunpeng_c_cpp_builder # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 字节对齐检查 ======'
    - devkit advisor byte-align -i 待扫描的软件包路径 -c 源码构建命令 -b 构建工具 -r html
    
    # 示例 devkit advisor byte-align -i /opt/DevKit/wtdbg2-2.5 -c make -b make -r html
    - mv ./byte-align*.html ./byte-alignment-check.html

  artifacts:
    paths:
      - byte-alignment-check.html
    name: byte-align

```
具体参数如下
| 参数  |  参数选项 | 参数说明  |
| ------------ | ------------ | ------------ |
| -i/--input  | package_path  |  必选参数。待扫描的源码文件夹路径，若存在多个扫描路径需使用英文逗号分割。。例如：/home/test1, /home/test2。 |
| -c/--cmd  |  cmd | 必选参数。源码构建命令。在服务器中正常执行的构建命令，命令中如有空格，要使用单引号包住。  |
| -b/--build-tool  |  make,cmake,automake | 必选参数。构建工具。当前工具支持make，cmake，automake，默认选项为make。 如-c make -b make 、-c cmake -b cmake 、-c make -b automake |
| -o/--output  |  output_path | 可选参数。报告存放路径。报告默认存放在当前执行路径下，名称默认为“特性名称_时间戳”。  |
| --set-timeout | time  |  可选参数。任务超时时间。默认无超时时间，任务将持续执行直到结束。 |
| -l/--log-level  | 0,1,2,3  |  可选参数。日志等级，0（DEBUG）、1（INFO）、2（WARNING）、3（ERROR），默认为1（INFO）。 |
| -r/--report-type  | all,json,html,csv  |  可选参数。扫描报告的格式。默认为all，即默认生成json、html、csv三种报告。 |


##### 内存一致性检查：
```
stages:  
  - build    
  - affinity-analysis

memory-consistency-check:
  stage: affinity-analysis
  tags:
    - kunpeng_c_cpp_builder  # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 内存一致性检查 ======'
    # 需编写生成的BC文件脚本
    - devkit advisor mem-cons -i BC文件对应的源码文件路径 -f BC文件路径 -r html
    
    # 示例 devkit advisor mem-cons -i /opt/DevKit/testcase/affinity/weak_cons/test-mulbc_sort -f /opt/DevKit/testcase/affinity/weak_cons/bc_file -r html
    - mv ./mem-cons*.html ./memory-consistency-check.html

  artifacts:
    paths:
      - memory-consistency-check.html
    name: mem-cons
```
具体参数如下
| 参数  |  参数选项 | 参数说明  |
| ------------ | ------------ | ------------ |
| -i/--input  | package_path  |  必选参数。BC文件对应的源码文件路径。例如：/home/test |
| -f/--bc-file  |  path | 必选参数。BC文件路径，该路径下必须存在BC文件。例如：/home/testbc  |
| --autofix  |  true/false |  可选参数。是否生成编译器配置文件。默认为false。 |
| --autofix-dir  | path  |  可选参数。编译器配置文件的存放地址。默认生成在工具目录下的源码文件夹下，使用--autofix且参数必须为true时才能生效。 |
| -o/--output  |  output_path | 可选参数。报告存放路径。报告默认存放在当前执行路径下，名称默认为“特性名称_时间戳”。  |
| --set-timeout | time  |  可选参数。任务超时时间。默认无超时时间，任务将持续执行直到结束。 |
| -l/--log-level  | 0,1,2,3  |  可选参数。日志等级，0（DEBUG）、1（INFO）、2（WARNING）、3（ERROR），默认为1（INFO）。 |
| -r/--report-type  | all,json,html,csv  |  可选参数。扫描报告的格式。默认为all，即默认生成json、html、csv三种报告。 |

##### 向量化检查：
```
stages:  
  - build    
  - affinity-analysis

vectorized-check:
  stage: affinity-analysis
  tags:
    - kunpeng_c_cpp_builder # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== 向量化检查 ======'
    # 需编写生成的BC文件脚本
    - devkit advisor vec-check -i BC文件对应的源码文件路径 -f BC文件路径 -c 源码的构建命令 -r html
      
    # 示例 devkit advisor vec-check -i /opt/DevKit/testcase/affinity/vec/simple -f /opt/DevKit/testcase/affinity/vec/BCfiles -c make -r html
    - mv ./vec-check*.html ./vectorized-check.html

  artifacts:
    paths:
      - vectorized-check.html
    name: vec-check

```
具体参数如下
|  参数 |  参数选项 | 参数说明  |
| ------------ | ------------ | ------------ |
| -i/--input  | package_path  | 必选参数。BC文件对应的源码文件路径。例如：/home/test  |
| -f/--bc-file  | path  |  必选参数。BC文件路径，该路径下必须存在BC文件。例如：/home/testbc |
| -c/--cmd |  cmd | 必选参数。源码的构建命令。例如：make all  |
| -p/--compiler  |  gcc,clang |  可选参数。编译器。默认为clang。例如：gcc。 |
| -o/--output  | output_path  | 可选参数。报告存放路径。报告默认存放在当前执行路径下，名称默认为“特性名称_时间戳”。  |
| --set-timeout  | time  | 可选参数。任务超时时间。默认无超时时间，任务将持续执行直到结束。  |
| -l/--log-level  | 0,1,2,3  |  可选参数。日志等级，0（DEBUG）、1（INFO）、2（WARNING）、3（ERROR），默认为1（INFO）。 |
| -r/--report-type  | all,json,html,csv  |  可选参数。扫描报告的格式。默认为all，即默认生成json、html、csv三种报告。 |
| --sve-enable  |  true,false |  可选参数。是否启用sve。默认不开启。 |


