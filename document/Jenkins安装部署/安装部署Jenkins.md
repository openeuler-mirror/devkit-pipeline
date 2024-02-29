<center><big><b>《安装部署 Jenkins》</b></big></center>



## 安装 Jenkins

> [Jenkins 离线安装 官方文档](https://www.jenkins.io/doc/book/installing/offline/)
>
> 如需获取目标系统的 Jenkins RPM 或 WAR 离线安装包可前往 [Jenkins mirrors](https://archives.jenkins.io/) 站点获取『推荐获取 **LTS Releases** 发行版』
>
> 例如本文使用的 Jenkins RPM 软件包下载链接 [https://archives.jenkins.io/redhat-stable/jenkins-2.426.2-1.1.noarch.rpm](https://archives.jenkins.io/redhat-stable/jenkins-2.426.2-1.1.noarch.rpm)
>
> 如您需要 Jenkins WAR 包,可前通过以下资源地址下载 [https://archives.jenkins.io/war-stable/2.426.2/jenkins.war](https://archives.jenkins.io/war-stable/2.426.2/jenkins.war)

 ### 1.配置 Jenkins YUM 源

   ```shell
   #=====================================================================================================================#
   # 下载 Jenkis YUM 镜像文件到 /etc/yum.repos.d/ 目录下『离线模式安装请查阅 Jenkins 官方文档』
   wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
   #---------------------------------------------------------------------------------------------------------------------#
   # 导入 Jenkins RPM 安装包校验证书
   rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
   #---------------------------------------------------------------------------------------------------------------------#
   # 升级系统软件包『可选(若需要执行以下命令,建议执行完后重启您的系统)』
   yum upgrade -y
   #=====================================================================================================================#
   ```

 ### 2.部署 Jenkins

   ```shell
   #=====================================================================================================================#
   # 安装 Jenkins RPM
   yum install jenkins -y
   #---------------------------------------------------------------------------------------------------------------------#
   # 重新加载 systemd 管理器配置
   systemctl daemon-reload
   #---------------------------------------------------------------------------------------------------------------------#
   # 设置开机启动服务并立即启动 jenkins.service
   systemctl --now enable jenkins.service
   #=====================================================================================================================#
   # 防火墙相关设置『可选(请根据实际情况配置您的防火墙,以允许访问 8080 端口)』
   firewall-cmd --permanent --zone=public --add-port=8080/tcp
   firewall-cmd --permanent --zone=public --add-service=http
   # 使用 RPM 安装的方式默认会安装此规则『若采用其他安装方式安装 Jenkins 请参考官方文档配置您的防火墙规则』
   firewall-cmd --permanent --zone=public --add-service=jenkins
   # 重新加载防火墙并保留状态信息
   firewall-cmd --reload
   # 查看防火墙规则设置是否生效
   firewall-cmd --permanent --zone=public --list-all
   #=====================================================================================================================#
   ```

 ### 3.Jenkins 初始化设置

   在浏览器端口键入以下地址访问 Jenkins 服务,并根据提示进行 Jenkins 的初始配置 **http://<服务器IP>:8080**

   - 根据提示获取初始密码并登录

     ```shell
     #=====================================================================================================================#
     # 获取初始密码参考命令
     cat /var/lib/jenkins/secrets/initialAdminPassword
     #=====================================================================================================================#
     ```

     填写初始密码,点击 **继续** 按钮

     ![Jenkins初始化设置01](./Jenkins.assets/Jenkins初始化设置01.png)

   - 配置代理『【可选】请根据您的网络环境进行设置,如需离线使用可点击 **跳过插件安装** 暂时跳过此步骤 (有关离线安装请查阅 [Jenkins离线安装官方文档](https://www.jenkins.io/doc/book/installing/offline/))』

     ![Jenkins初始化设置02](./Jenkins.assets/Jenkins初始化设置02.png)

   - 点击 配置代理 按钮进入下图界面配置代理,配置完成后点击 **保存并继续** 按钮

     ![Jenkins初始化设置03](./Jenkins.assets/Jenkins初始化设置03.png)

   - 安装推荐的插件『【可选】如您的网络不可用,可跳过以下步骤。点击 **选择插件来安装** 根据提示,跳过安装』

     ![Jenkins初始化设置04](./Jenkins.assets/Jenkins初始化设置04.png)

   - 配置管理员用户登录信息『请根据自己的实际需求填写信息,填写完成后请点击 **保存并完成** 按钮』

     ![Jenkins初始化设置05](./Jenkins.assets/Jenkins初始化设置05.png)

   - Jenkins 实例配置『请根据自己的实际需求填写,此处使用默认设置.填写完成后请点击 **保存并完成** 按钮』

     ![Jenkins初始化设置06](./Jenkins.assets/Jenkins初始化设置06.png)

   - Jenkins安装已完成『请点击 **开始使用Jenkins** 按钮』

     ![Jenkins初始化设置07](./Jenkins.assets/Jenkins初始化设置07.png)

4. ### Jenkins 基础插件安装『插件的离线安装方式请访问插件主页下载符合目标要求的插件安装包 (.hpi)』

   > 有关 Jenkins 插件管理请阅读以下文章： [管理插件 (jenkins.io)](https://www.jenkins.io/doc/book/managing/plugins/)

   - #### 安装 [Blue Ocean](https://plugins.jenkins.io/blueocean/) 插件『可选 (以更直观的的方式查看 pipeline 状态)』

     ![安装BlueOcean插件01](./Jenkins.assets/安装BlueOcean插件01.png)![安装BlueOcean插件02](./Jenkins.assets/安装BlueOcean插件02.png)

   - #### 安装 [Safe Restart](https://plugins.jenkins.io/saferestart/) 插件『可选 (安全重启 Jenkins)』

     ![安装SafeRestart插件01](./Jenkins.assets/安装SafeRestart插件01.png)![安装SafeRestart插件02](./Jenkins.assets/安装SafeRestart插件02.png)

  