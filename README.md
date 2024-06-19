# devkit-pipeline

#### 介绍
Pipeline solution to support building, deploying and automating any software project on Kunpeng Architecture

#### 软件架构
支持x86/鲲鹏多样性算力下openEuler系操作系统

#### 使用说明
##### 用户指南链接
https://www.hikunpeng.com/document/detail/zh/nativedevp/userguide/devkitpipepre/NativeDevelopment_0001.html
##### 原生开发流水线工具安装部署

**注：离线安装可查看用户指南**

###### 1.下载原生开发流水线工具安装包并解压

```
wget -c https://gitee.com/openeuler/devkit-pipeline/releases/download/v1.0.2/devkit-pipeline-v1.0.2.tar.gz && tar -xzvf devkit-pipeline-v1.0.2.tar.gz
```
###### 2.进入`devkit-pipeline-v1.0.2/linux`文件夹，根据实际情况修改machine.yaml配置文件，以下是具体配置项说明

```shell
说明：

user：当前节点连接其他节点配置免密的用户名。
pkey：配置免密所用公钥对应的私钥路径。

scanner、java_builder_jdk8、java_builder_jdk17、c_builder_gcc、c_builder_bisheng_compiler、compatibility、tester、devkit、clamav均为角色名称，大致与组件一一对应，需以yaml列表的形式修改角色对应的IP地址。

```
###### 3.配置免密

```shell
ssh-keygen -t rsa
ssh-copy-id -i  ${HOME}/.ssh/id_rsa.pub USER@REMOTE_HOST
```

###### 4.配置sudo免密
执行visudo命令修改“/etc/sudoers”文件。
普通用户（USER）配置sudo权限，需在“/etc/sudoers”文件中对比“root ALL=(ALL) ALL”新增一行。

```
root    ALL=(ALL)       ALL 
USER    ALL=(ALL)       NOPASSWD: ALL
```
普通用户（USER）配置sudo免密，需在“ /etc/sudoers”文件中对比 “# %wheel ALL=(ALL) NOPASSWD: ALL”新增一行。

```
# %wheel        ALL=(ALL)       NOPASSWD: ALL 
USER            ALL=(ALL)       NOPASSWD: ALL
```
###### 5.安装部署前安装关键倚赖

```shell
yum install tar -y

yum install perf -y    (A-FOT组件必备)
yum install clamav -y  (ClamAV组件必备)
```
###### 6.安装部署原生开发组件（关键命令）
可按machine.yaml配置文件中的有效角色部署GCCforOpenEuler、BiSheng Compiler、BiSheng JDK8、BiSheng JDK17、Compatibility Testing、DevKit Web、DevKit CLI、DevKit Tester、ClamAV组件。

```
./deploy_tool -f machine.yaml
```
注：部署工具兼有组件下载功能。若批量部署工具所在节点网络不通时，可以使用download_tool一键下载工具在有网络的机器上先行下载所需的组件安装包，再上传至所需节点使用。详见用户指南。
###### 7.部署完成后，可在`${HOME}/.local`和$`{HOME}/.bashrc`中查看部署结果

##### Docker镜像生成
1. [测试平台&Java性能测试工具Docker镜像生成样例](./document/DockerFile配置/Docker镜像生成手册.md)
2. [迁移亲和门禁Docker镜像生成样例](./document/迁移&亲和Dockerfile/Docker镜像生成手册.md)

#### 参与贡献
    如果您想为本仓库贡献代码，请向本仓库任意maintainer发送邮件
    如果您找到产品中的任何Bug，欢迎您提出ISSUE
