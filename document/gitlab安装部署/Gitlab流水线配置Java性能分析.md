## gitlab中集成Java性能分析

### 一. Java性能分析

```
stages:  
  - build    
  - test
  - deploy

source-code-migration:
  stage: build
  tags:
    - kunpeng_builder # 对应gitlab-runner注册时的标签，可选择多个
  script:
    - echo '====== Java Performance Analysis ======'
    - CURDIR=$(pwd)
    # 删除上次jmeter产生的报告 (jmeter 命令-l、-o指定的文件和路径)
    - rm -rf /home/zpp/report /home/zpp/result.html 
    # 运行java性能采集
    - /home/zpp/.local//devkit_distribute/bin/entrance -i 160.0.1.2,160.0.1.3 -u root -f /home/zpp/.ssh/id_rsa -D 160.0.1.5 -a spring-boot -d 10 -g /home/zpp/spring-boot -j "sh /home/zpp/apache-jmeter-5.6.3/bin/jmeter.sh -nt /home/zpp/Test_request.jmx -l /home/zpp/result.html -eo /home/zpp/report"
    - cp /home/zpp/.local/devkit_distribute/data/devkit_distribute-defaults.yaml ${CURDIR}
  artifacts:
    paths:
      # 上传报告
      - devkit_distribute-defaults.yaml  # 文件后缀.html
      name: Java_Performance_Report

```

**entrance**具体参数如下

| 参数 | 参数类型      | 参数说明                                                                                                                                   |
|----|-----------|----------------------------------------------------------------------------------------------------------------------------------------|
| -i | ipv4,ipv4 | 必选参数。需要采集的目标程序所在的服务器地址， 多个使用逗号隔离                                                                                                       |
| -u | str       | 必选参数。服务器的用户名                                                                                                                           |
| -f | str       | 必选参数。执行机免密登陆所有服务器（-i指定的）的私钥路径                                                                                                          |
| -a | str       | 必选参数。需要采集的应用名称，多个采用逗号隔离                                                                                                                |
| -g | str       | 可选参数。执行机上通过git clone下载的代码路径                                                                                                            |
| -j | str       | 可选参数。jmeter执行命令。例如 bash /opt/apache-jmeter-5.6.3/bin/jmeter.sh -nt /home/xxx/Request.jmx -l /home/xxx/result.html -eo /home/xxx/report |
| -d | num       | 必选参数。任务采集执行时间，单位秒，当存在-j参数时，jmeter结束或者到达采集执行时间，结束采集。                                                                                    |
| -D | ipv4      | 必选参数。Devkit工具的地址。                                                                                                                      |
| -P | num       | 可选参数。Devkit工具的端口，默认值8086。                                                                                                              |
| -U | str       | 可选参数。Devkit工具的用户名，默认值devadmin。                                                                                                         |
| -W | str       | 可选参数。Devkit工具的密码，默认值devkit123。                                                                                                         |

### 二. 配置示例

#### 1. 安装java分发采集命令行工具到执行jenkins执行机

##### 1.1 使用deploy_tool命令安装角色executor

[通过deploy_tool部署工具部署executor](../批量部署工具/批量部署工具和一键下载工具说明文档.md)

安装完成后查看

![安装成功](./DevkitPerformanceAnalysis.assets/安装成功.png)

##### 1.2 离线安装

###### 1.2.1 下载离线包

发行版中下载<font color=white>**最新**</font>的devkit_distribute.tar.gz
![下载Devkit_Distribute](DevkitPerformanceAnalysis.assets/下载Devkit_Distribute.png)

###### 1.2.2 执行以下命令：

```shell
  mkdir -p "${HOME}"/.local
  tar --no-same-owner -zxf devkit_distribute.tar.gz -C "${HOME}"/.local/
```

###### 1.2.3 安装成功：

![Devkit_Distribute离线安装成功.png](DevkitPerformanceAnalysis.assets/Devkit_Distribute离线安装成功.png)

#### 2. 确定需要采集的java程序所在机器存在jcmd命令

##### 2.1 检查jcmd命令是否存在

![确定是否存在jcmd命令](./DevkitPerformanceAnalysis.assets/检查jcmd命令存在.png)

##### 2.2 安装jcmd命令

![安装带有jcmd命令的JDK](./DevkitPerformanceAnalysis.assets/安装带有jcmd命令的JDK.png)

#### 3. 配置流水线

![创建Pipeline任务01](./DevkitPerformanceAnalysis.assets/01_创建流水线.png)
![创建Pipeline任务02](./DevkitPerformanceAnalysis.assets/02_编写流水线.png)

----

#### 4. 执行任务

![执行任务](./DevkitPerformanceAnalysis.assets/08_流水线执行.png)

![执行任务](./DevkitPerformanceAnalysis.assets/09_流水线执行.png)

----

#### 5. 查看流水线执行状态和报告

##### 5.1查看流水线结果

![执行结束打开报告](./DevkitPerformanceAnalysis.assets/03_查看流水线.png)

##### 5.2 流水线失败

![执行结束打开报告](./DevkitPerformanceAnalysis.assets/04_流水线失败状态.png)

![执行结束打开报告](./DevkitPerformanceAnalysis.assets/05_流水线失败原因.png)

##### 5.3 流水线成功

![具体报告](./DevkitPerformanceAnalysis.assets/06_流水线执行成功.png)

##### 5.4 下载的报告

![具体报告](./DevkitPerformanceAnalysis.assets/07_下载的最终报告.png)
 