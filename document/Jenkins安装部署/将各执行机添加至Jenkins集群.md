<center><big><b>《将各执行机添加至Jenkins集群》</b></big></center>





### 凭证设置

   添加凭据域

   ![添加鲲鹏DevKitJenkinsCI插件凭据域01](./Jenkins.assets/添加鲲鹏DevKitJenkinsCI插件凭据域01.png)
   域名和描述按需输入，便于识别和管理即可
   ![添加鲲鹏DevKitJenkinsCI插件凭据域02](./Jenkins.assets/添加凭据域.png)

   添加凭据到凭据域下

   ![添加凭据到DevKit凭据域下](./Jenkins.assets/凭据02.png)


   在 DevKit 凭据域下添加 aarch64 Jenkins 工作节点 SSH 凭据

   ```shell
   #=====================================================================================================================#
   # 在安装了 Jenkins 的环境上生成工作节点 SSH 免密登录证书『请根据实际需求设置 SSH key passphrases』
   ssh-keygen -b 4096 -C "<邮件地址或其他标签>" -f ~/.ssh/id_ed25519_<推荐按照 *_*_*_* 格式填写目标服务器IP,便于管理 KEY> -t ed25519
   #---------------------------------------------------------------------------------------------------------------------#
   # 在安装了 Jenkins 的环境上将生成的证书的公钥上传至目标服务器『请根据提示输入目标服务器对应账户密码』
   ssh-copy-id -i ~/.ssh/id_ed25519_<推荐按照 *_*_*_* 格式填写目标服务器IP,便于管理 KEY>.pub root@<目标服务器IP>
   #---------------------------------------------------------------------------------------------------------------------#
   # 删除已知主机名文件中属于指定主机名的所有密钥
   ssh-keygen -R <目标服务器IP>
   #---------------------------------------------------------------------------------------------------------------------#
   # 使用 SSH key 测试连接目标主机『如果您设置了 SSH key passphrases,请在连接目标主机时根据提示输入证书密码』
   ssh -o IdentitiesOnly=yes -o PasswordAuthentication=no -i ~/.ssh/id_ed25519_<推荐按照 *_*_*_* 格式填写目标服务器IP,便于管理 KEY> -l root -p 22 <目标服务器IP>
   #---------------------------------------------------------------------------------------------------------------------#
   # 查看 SSH key 私钥
   cat ~/.ssh/id_ed25519_<推荐按照 *_*_*_* 格式填写目标服务器IP,便于管理 KEY>
   #=====================================================================================================================#
   ```

   ![在DevKit凭据域下添加aarch64Jenkins工作节点SSH凭据01](./Jenkins.assets/在DevKit凭据域下添加aarch64Jenkins工作节点SSH凭据01.png)![在DevKit凭据域下添加aarch64Jenkins工作节点SSH凭据02](./Jenkins.assets/在DevKit凭据域下添加aarch64Jenkins工作节点SSH凭据02.png)

 ### 工作节点设置

   ![工作节点设置01](./Jenkins.assets/工作节点设置01.png)![工作节点设置02](./Jenkins.assets/工作节点设置02.png)


   |  配置项 | 配置说明  |
| ------------ | ------------ |
| 名字  |  与节点名称保持一致 |
| 描述  |  按需填写,便于管理标识和即可，如aarch64node(ip)openeuler22.03 |
| Number of executors  |  默认为1 |
| 远程工作目录  |  /home/JenkinsWorkspace/ |
| 标签  | 流水线脚本中根据标签来选取执行机，可以打多个标签，用空格隔开，标签需要以kunpeng_为前缀，如kunpeng_scanner kunpeng_builder kunpeng_executor  |
| 用法  | Only build jobs with label expressions matching this node  |
| 启动方式  |  Launch agents via SSH |
| 主机  | 节点IP  |
| Credentials  | 已添加的凭据  |
| Host Key Verification Strategy  | Known hosts file Verification Strategy  |
| 可用性  |  Keep this agent online as much as possible |
| 节点属性(可选) | 若需要配置环境变量可选择Environment variables  |

![工作节点设置03](./Jenkins.assets/工作节点设置05.PNG)![工作节点设置04](./Jenkins.assets/工作节点设置04.png)

### FAQ

   ![Jenkins工作节点连接失败FAQ](./Jenkins.assets/Jenkins工作节点连接失败FAQ.png)

   > 当 Jenkins 工作节点连接不上时,且查看日志如上图所示时请考虑通过以下解决方案解决此问题。
   >
   > 有关 Jenkins 工作节点连接错误的问题可参考以下文章中 poddingue 的解答：[https://community.jenkins.io/t/node-connection-error/6082](https://community.jenkins.io/t/node-connection-error/6082)

   - 临时断开节点

     ![临时断开节点](./Jenkins.assets/临时断开节点.png)

   - 在安装 Jenkins 服务的设备上配置 /var/lib/jenkins/.ssh/known_hosts

     ```shell
     #=====================================================================================================================#
     # 创建目标 /var/lib/jenkins/.ssh 目录
     mkdir -p /var/lib/jenkins/.ssh
     #---------------------------------------------------------------------------------------------------------------------#
     # 新建 known_hosts 文件
     touch /var/lib/jenkins/.ssh/known_hosts
     #---------------------------------------------------------------------------------------------------------------------#
     # 修改 known_hosts 文件权限为 600
     chmod 600 /var/lib/jenkins/.ssh/known_hosts
     #---------------------------------------------------------------------------------------------------------------------#
     # 将远程主机的 SSH 主机密钥添加到 known_hosts 文件中
     ssh-keyscan <目标服务器IP> >> /var/lib/jenkins/.ssh/known_hosts
     #---------------------------------------------------------------------------------------------------------------------#
     # 修改文件夹下所有文件的所属用户及用户组为 jenkins
     chown -R jenkins:jenkins /var/lib/jenkins/.ssh
     #=====================================================================================================================#
     ```

   - 重新连接节点

     ![image-20240104214716242](./Jenkins.assets/重新连接节点.png)

   - 节点连接成功后如下图所示

     ![节点连接成功](./Jenkins.assets/节点连接成功.png)
