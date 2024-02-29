<p style="text-align: center;font-size: 32px">
<strong>安装部署 gitlab</strong>
</p>


#### 1.下载gitlab安装包
```
wget https://mirrors.tuna.tsinghua.edu.cn/gitlab-ce/yum/el7/gitlab-ce-16.9.1-ce.0.el7.x86_64.rpm
```
#### 2.安装gitlab
```
yum install -y git tar policycoreutils-python openssh-serve
rpm -ivh gitlab-ce-16.9.1-ce.0.el7.x86_64.rpm
```
安装成功会有如下信息：
![](./gitlab/安装01.PNG) 
#### 3.修改IP端口
编辑gitlab配置文件

```
vi /etc/gitlab/gitlab.rb
```
修改对外访问的Ip端口：
```
external_url 'http://localhost:8081' # 修改成 http://实际ip:需要访问的端口
```
![](./gitlab/访问地址.PNG) 
```
设置的端口不能被占用，如设置的端口已经使用，请自定义其它端口，并在防火墙设置开放范围内的端口
a.	执行systemctl status firewalld命令查看服务器OS防火墙的状态。如果防火墙已开启（active），执行如下操作开通防火墙端口；如果防火墙没有开启（inactive），请跳过以下步骤。
b.	执行firewall-cmd --query-port=8081/tcp命令查看端口是否开通，提示“no”表示端口未开通。
c.	执行firewall-cmd --add-port=8081/tcp --permanent命令永久开通端口，提示“success”表示开通成功。
d.	执行firewall-cmd --reload命令重新载入配置。
e.	再次执行firewall-cmd --query-port=8081/tcp命令查看端口是否开通，提示“yes”表示端口已开通。
```
#### 4.重新加载配置

```
gitlab-ctl reconfigure     #重新生成相关配置文件，执行此命令时间比较长
```
![](./gitlab/重新配置.PNG) 

#### 5.配置gitlab开机自动启动

```
systemctl enable gitlab-runsvdir.service
systemctl start gitlab-runsvdir.service
# 关闭gitlab的自动启动命令：systemctl disable gitlab-runsvdir.service
```
#### 6.启动gitLab

```
gitlab-ctl restart      
```
启动成功会有如下信息
![](./gitlab/启动01.PNG) 
#### 7.查看 gitlab 版本

```
cat /opt/gitlab/embedded/service/gitlab-rails/VERSION # 回显应为16.9.1

```

#### 8.页面访问 gitlab 

```
http://ip:8081/ # 端口根据个人配置进行更改
```
![](./gitlab/登录01.PNG) 
#### 9.登录 gitlab 

默认账户名是root,密码存放在配置文件 /etc/gitlab/initial_root_password

![](./gitlab/密码01.PNG) 


#### 10.设置为简体中文

先登录gitLab，登录成功后，在gitlab后台的系统设置里配置简体中文。


![](./gitlab/中文01.PNG) 
完成后刷新页面即可

![](./gitlab/中文02.PNG) 