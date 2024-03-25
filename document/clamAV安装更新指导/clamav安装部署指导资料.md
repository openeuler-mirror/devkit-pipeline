# 基于openEuler官方yum源安装部署使用clamav工具指导

[TOC]



## 安装clamav病毒扫描工具

通过yum源安装clamav工具

```shell
yum install -y clamav
```

该命令会安装clamav, clamav-data, clamav-filesystem三个组件

## 手动更新方案

1. 通过能连接公网的环境使用下述连接下载daily.cvd文件

```shell
https://database.clamav.net/daily.cvd
https://database.clamav.net/main.cvd
https://database.clamav.net/bytecode.cvd
```


2. 将daily.cvd文件复制到/var/lib/clamav目录下


## 自动更新（需要能访问公网）
通过yum源安装clamav-update组件

```shell
yum install -y clamav-update
```

该组件设定好了定时任务，每隔8小时自动下载最新病毒库
安装该组件后也可通过执行命令更新病毒库

```shell
freshclam
```

![](.\image\更新病毒库.png)

命令参数说明

| 是否常用 | 参数                          | 释义                                                         |
| -------- | ----------------------------- | ------------------------------------------------------------ |
| 是       | clamscan --helpe              | 查看帮助                                                     |
| 是       | -log=FILE                     | 将扫播报告保存到FILE                                         |
| 是       | -r                            | 递归扫描,即扫描指定目录下的子目录                            |
| 是       | -copy=DIRECTORY               | 将受感染的文件复制到DIRECTORY中                              |
| 是       | -i                            | 仅仅打印被感染的文件                                         |
| 是       | -quiet                        | 仅输出错误消息                                               |
|          | -official-db-only[=yes/no(*)] | 只加载官方签名                                               |
|          | -max-filesize In              | 将跳过大于此的文件并假定为于净                               |
|          | -max-scansize I/n             | 要扫描每个容器文件的最人数据量                               |
|          | -leave-temps[=yes/no(*)]      | 不要删除临时文件                                             |
|          | -file-list-FILE               | 从文件中扫描文件                                             |
|          | -bell                         | 病毒检测的响铃                                               |
|          | -cross-fs[=yes(*/no]s         | 扫描其他文件系统上的文件和目录                               |
|          | bytecode-timeout=N            | 设置字节码超时(以毫秒为单位)                                 |
|          | -heuristic-alerts[=yes(*}/no] | 切换启发式警报                                               |
|          | -alert-encrypted[=yes/no(*)]  | 警告加密档案和文件                                           |
|          | -nocertse                     | 在PE文件中禁用authenticode证书链验证                         |
|          | -disable-cachee               | 禁用继存和缓存检查扫描文件的哈希值                           |
|          | -d<文件>                      | 以指定的文件作为病毒库,一代替默认的/var/clamav目录下的病毒库文件 |
|          | -l<文件>                      | 指定日志文件,以代替默认的/var/og/damav/freshclam.log文件     |
|          | -move=<目录>                  | 把感染病审的文件移动到指定目录                               |
|          | -removes                      | 删除感染病毒的文件                                           |



## 更新病毒库前后结果对比

扫描命令示例
```shell
clamscan -i -r /var/log/ -l clamav.log
```
![](.\image\病毒扫描结果.png)

## Jenkins集成clamAV:

如下示例代码，在流水线上配置递归扫描/home路径下的病毒，并输出扫描日志到家路径下的clamscan.log中

```groovy
pipeline {
    agent any
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    stages{
        stage('freshclam') {
            agent {
                label 'Linux_aarch64'
            }
            steps{
				sh 'freshclam'
            }
        }
        stage('clamscan') {
            agent {
                label 'Linux_aarch64'
            }
            steps{
				sh 'clamscan -i -r /home -l ~/clamscan.log'
            }
        }
    }
}
```

