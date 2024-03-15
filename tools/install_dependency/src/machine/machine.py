import os
import paramiko
import socket
import logging
import timeout_decorator

import constant
from command_line import CommandLine
from exception.connect_exception import CreatePkeyFailedException, ConnectRemoteException, \
    NotMatchedMachineTypeException
from download import component_collection_map
from utils import base_path

LOGGER = logging.getLogger("install_dependency")
SHELL_FILE_LIST = ["install.sh", "check_install_result.sh"]


class Machine:
    def __init__(self, ip, user, pkey, password=None):
        self.ip = ip
        self.user = user
        self.pkey = pkey
        self.password = password
        self.check_is_aarch64()

    def check_is_aarch64(self):
        machine_type = self.get_machine_type()
        LOGGER.info(f"{self.ip} machine type: {machine_type}")
        if machine_type != "aarch64":
            LOGGER.error(f"Machine type of {self.ip} is {machine_type}, not aarch64. Please replace this machine.")
            raise NotMatchedMachineTypeException()

    def get_machine_type(self):
        try:
            ssh_client = self.ssh_client()
            stdin, stdout, stderr = ssh_client.exec_command("uname -m", timeout=10)
        except (paramiko.ssh_exception.SSHException, socket.timeout) as e:
            LOGGER.error(f"Connect remote {self.ip} failed. {str(e)}")
            raise ConnectRemoteException()
        stdout_output = stdout.read().decode().strip()
        ssh_client.close()
        return stdout_output

    def ssh_client(self):
        ssh = paramiko.SSHClient()
        ssh._transport = self.transport_connect(self.user, self.pkey, self.password)
        return ssh

    def sftp_client(self):
        sftp = paramiko.SFTPClient.from_transport(self.transport_connect(self.user, self.pkey, self.password))
        return sftp

    def transport_connect(self, user, pkey_path, password=None):
        try:
            # 指定本地的RSA私钥文件。如果建立密钥对时设置的有密码，password为设定的密码，如无不用指定password参数
            pkey = paramiko.RSAKey.from_private_key_file(pkey_path, password=password)
        except (IOError,) as e:
            LOGGER.error(f"Pkey file not exists. {str(e)}")
            raise CreatePkeyFailedException()
        except (paramiko.ssh_exception.PasswordRequiredException, paramiko.ssh_exception.AuthenticationException) as e:
            LOGGER.warning(f"Pkey password is required. {str(e)}")
            password = input(f"Press Enter to input password of {pkey_path}: ")
            self.password = password
            return self.transport_connect(user, pkey_path, password)
        except (paramiko.ssh_exception.SSHException,) as e:
            LOGGER.error(f"Connect remote {self.ip} failed because of wrong pkey. {str(e)}")
            raise CreatePkeyFailedException()

        try:
            transport = self.transport_connect_with_timeout(user, pkey)
        except (paramiko.ssh_exception.AuthenticationException,
                paramiko.ssh_exception.SSHException,
                timeout_decorator.TimeoutError,
                socket.gaierror,
                socket.timeout,
                socket.error) as e:
            LOGGER.error(f"Connect remote {self.ip} failed. {str(e)}")
            raise ConnectRemoteException()
        return transport

    @timeout_decorator.timeout(10)
    def transport_connect_with_timeout(self, user, pkey):
        transport = paramiko.Transport((self.ip, 22))
        transport.connect(username=user, pkey=pkey)
        return transport

    def install_component(self, component_name):
        ssh_client = self.ssh_client()
        sftp_client = self.sftp_client()
        try:
            self.install_component_handler(component_name, sftp_client, ssh_client)
        except (FileNotFoundError, PermissionError, NotADirectoryError, OSError, IOError) as e:
            LOGGER.error(f"Remote machine {self.ip} occur Error: {str(e)}")
        finally:
            ssh_client.close()
            sftp_client.close()

    def install_component_handler(self, component_name, sftp_client, ssh_client):
        component_name_to_func_dict = {
            "GCCforOpenEuler": self.default_install_component_handle,
            "BiShengCompiler": self.default_install_component_handle,
            "BiShengJDK17": self.default_install_component_handle,
            "BiShengJDK8": self.default_install_component_handle,
            "OpenEulerMirrorISO": self.deploy_iso_handle,
        }
        return component_name_to_func_dict.get(component_name)(component_name, sftp_client, ssh_client)

    def deploy_iso_handle(self, component_name, sftp_client, ssh_client):
        # 上传 镜像文件
        LOGGER.info(f"Deploy component in remote machine {self.ip}: {component_name}")
        local_path = os.path.abspath(CommandLine.iso_path)
        remote_path = os.path.join("/home", local_path.split('/')[-1])
        LOGGER.debug(f"Transport local_file: {local_path} to remote machine {self.ip} "
                     f"remote_file: {remote_path}")
        sftp_client.put(localpath=local_path, remotepath=remote_path)

        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        remote_file_list = []
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", component_name + shell_file)
            sh_cmd = f"bash {sh_file_remote_path} {remote_path}"
            execute_output = (
                self.transport_shell_file_and_execute(
                    ssh_client, sftp_client,
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            remote_file_list.append(sh_file_remote_path)
            if shell_file == SHELL_FILE_LIST[1]:
                install_result = execute_output

        if install_result == "true":
            LOGGER.info(f"Remote machine {self.ip} deploy {component_name} success.")
        else:
            LOGGER.info(f"Remote machine {self.ip} deploy {component_name} failed.")
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)

    def default_install_component_handle(self, component_name, sftp_client, ssh_client):
        try:
            stdin, stdout, stderr = ssh_client.exec_command(f"mkdir -p /tmp/{constant.DEPENDENCY_DIR}", timeout=10)
        except (paramiko.ssh_exception.SSHException, socket.timeout) as e:
            LOGGER.error(f"Connect remote {self.ip} failed. {str(e)}")
            raise ConnectRemoteException()
        exit_status = stdout.channel.recv_exit_status()
        LOGGER.debug(f"Remote machine {self.ip} mkdir -p /tmp/{constant.DEPENDENCY_DIR} result: "
                     f"{'success' if not exit_status else 'failed'}")
        if exit_status:
            raise NotADirectoryError(f"Remote machine {self.ip} "
                                     f"directory {os.path.join('/tmp/', constant.DEPENDENCY_DIR)} not exist.")

        # 上传 组件压缩包和校验文件
        LOGGER.info(f"Install component in remote machine {self.ip}: {component_name}")
        remote_file_list = []
        shell_dict = component_collection_map.get(component_name)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            component = url_and_save_path.get("save_path")
            LOGGER.debug(f"Transport component file to remote machine {self.ip}: {component}")
            remote_file = os.path.abspath(os.path.join('/tmp', component))
            remote_file_list.append(remote_file)
            sftp_client.put(localpath=f"{component}", remotepath=f"{remote_file}")

        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", constant.DEPENDENCY_DIR, component_name + shell_file)
            sh_cmd = f"bash {sh_file_remote_path}"
            execute_output = (
                self.transport_shell_file_and_execute(
                    ssh_client, sftp_client,
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            remote_file_list.append(sh_file_remote_path)
            if shell_file == SHELL_FILE_LIST[1]:
                install_result = execute_output

        if install_result == "true":
            LOGGER.info(f"Remote machine {self.ip} install {component_name} success.")
        else:
            LOGGER.info(f"Remote machine {self.ip} install {component_name} failed.")
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)

    def transport_shell_file_and_execute(self, ssh_client, sftp_client, sh_file_local_path, sh_file_remote_path, sh_cmd):
        if not os.path.exists(sh_file_local_path):
            LOGGER.error(f"{sh_file_local_path} not exists.")
            raise FileNotFoundError(f"local file {sh_file_local_path} not exists.")

        LOGGER.debug(f"Transport local_file: {sh_file_local_path} to remote machine {self.ip} "
                     f"remote_file: {sh_file_remote_path}")
        sftp_client.put(localpath=sh_file_local_path, remotepath=sh_file_remote_path)

        stdin, stdout, stderr = ssh_client.exec_command(sh_cmd)
        output = stdout.read().decode().strip()
        LOGGER.info(f"Remote machine {self.ip} '{sh_cmd}' file output: {output}")
        return output

    def clear_tmp_file_at_remote_machine(self, ssh_client, remote_file_list):
        LOGGER.debug(f"Clear tmp file at remote machine {self.ip}")
        for remote_file in remote_file_list:
            LOGGER.debug(f"Delete tmp file at remote machine {self.ip}: {remote_file}")
            ssh_client.exec_command(f"rm -f {remote_file}")