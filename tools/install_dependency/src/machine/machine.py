import logging
import os
import socket
import subprocess
import typing

import paramiko
import timeout_decorator

import constant
from deploy.deploy_command_line import CommandLine
from exception.connect_exception import (CreatePkeyFailedException, ConnectRemoteException,
                                         NotMatchedMachineTypeException)
from download.download_utils import component_collection_map
from utils import (base_path, validate_path, MKDIR_TMP_DEVKITDEPENDENCIES_CMD,
                   CHECK_TAR_AVAILABLE_CMD, YUM_INSTALL_LKP_DEPENDENCIES_CMD,
                   CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR, CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE,
                   CHECK_MIRROR_INSTALL_STATUS, PROMPT_MAP)

LOGGER = logging.getLogger("install_dependency")
SHELL_FILE_LIST = ["install.sh", "check_install_result.sh"]


class Machine:
    def __init__(self, ip, user, pkey, password=None):
        self.ip = ip
        self.user = user
        self.pkey = pkey
        self.password = password
        self.check_is_aarch64()
        self.component_list = []
        self.mirrors = False

    def add_component(self, component):
        self.component_list.extend(component)
        self.component_list = list(set(self.component_list))

    def get_components(self):
        return self.component_list.copy()

    def set_mirror(self):
        self.mirrors = True

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
        if not validate_path(pkey_path) or not os.path.isfile(pkey_path):
            LOGGER.error("Yaml file content not correct. Given pkey not exists.")
            raise ConnectRemoteException()
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
        except timeout_decorator.TimeoutError as e:
            LOGGER.error(f"Remote machine {self.ip} occur Error: Exec cmd {str(e)}")
        except (FileNotFoundError, PermissionError, NotADirectoryError, OSError, IOError) as e:
            LOGGER.error(f"Remote machine {self.ip} occur Error: {str(e)}")
        finally:
            ssh_client.close()
            sftp_client.close()

    def install_components(self):
        if self.mirrors:
            self.install_component("OpenEulerMirrorISO")
        for component in self.component_list:
            self.install_component(component)
        if self.mirrors:
            self.install_component("UnOpenEulerMirrorISO")

        self.clear_tmp_file_at_remote_machine(
            self.ssh_client(), [os.path.join("/tmp/", constant.DEPENDENCY_DIR)])

    def install_component_handler(self, component_name, sftp_client, ssh_client):
        component_name_to_func_dict: typing.Dict[
            str, typing.Callable[[str, paramiko.SFTPClient, paramiko.SSHClient], typing.Any]] = {
            "GCCforOpenEuler": self.default_install_component_handle,
            "BiShengCompiler": self.default_install_component_handle,
            "BiShengJDK17": self.default_install_component_handle,
            "BiShengJDK8": self.default_install_component_handle,
            "LkpTests": self.lkptest_install_component_handle,
            "NonInvasiveSwitching": self.nis_install_component_handle,
            "DevKitWeb": self.devkitweb_install_component_handle,
            "OpenEulerMirrorISO": self.deploy_iso_handle,
            "UnOpenEulerMirrorISO": self.undeploy_iso_handle,
            "A-FOT": self.install_a_fot,
        }
        self._remote_exec_command(CHECK_TAR_AVAILABLE_CMD, ssh_client)
        self._remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, ssh_client)
        self._remote_exec_command(CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE, ssh_client)
        return component_name_to_func_dict.get(component_name)(component_name, sftp_client, ssh_client)

    def devkitweb_install_component_handle(self, component_name, sftp_client, ssh_client):
        # 上传 tar.gz 文件
        LOGGER.info(f"Install component in remote machine {self.ip}: {component_name}")
        remote_file_list = []
        shell_dict = component_collection_map.get(component_name)

        url_and_save_path = shell_dict.get("download file")
        local_file = url_and_save_path.get("save_path")
        remote_file = os.path.abspath(os.path.join('/opt', local_file.split('/')[-1]))
        LOGGER.debug(f"Transport local_file: {local_file} to remote machine {self.ip} "
                     f"remote_file: {remote_file}")
        remote_file_list.append(remote_file)
        sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")

        cmd = f"{os.path.join(base_path('component'), component_name, 'devkit_installer')} " \
              f"-i {self.ip} -u {self.user} -p {self.pkey} -paname {local_file.split('/')[-1]}"
        LOGGER.debug(f"Executing command: {cmd}")
        subprocess.run(cmd.split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)

        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)

    def install_a_fot(self, component_name, sftp_client, ssh_client):
        self._remote_exec_command("sudo yum install -y perf", ssh_client)
        saved_path = os.path.join(constant.DEFAULT_PATH, "a-fot.tar.gz")
        remote_file = os.path.abspath(os.path.join('/tmp', saved_path))
        LOGGER.debug(f"Transport local_file: {saved_path} to remote machine {self.ip} "
                     f"remote_file: {remote_file}")
        sftp_client.put(localpath=f"{saved_path}", remotepath=f"{remote_file}")
        self.nis_install_component_handle(component_name, sftp_client, ssh_client)
        self.clear_tmp_file_at_remote_machine(ssh_client, [remote_file])

    def nis_install_component_handle(self, component_name,  sftp_client, ssh_client):
        remote_file_list = []
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
            LOGGER.error(f"Remote machine {self.ip} install {component_name} failed.")
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)

    def lkptest_install_component_handle(self, component_name, sftp_client, ssh_client):
        self._remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, ssh_client)
        self._remote_exec_command(YUM_INSTALL_LKP_DEPENDENCIES_CMD, ssh_client)

        # 上传 lkp-tests.tar.gz文件
        LOGGER.info(f"Install component in remote machine {self.ip}: {component_name}")
        remote_file_list = []
        shell_dict = component_collection_map.get(component_name)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            local_file = url_and_save_path.get("save_path")
            remote_file = os.path.abspath(os.path.join('/tmp', constant.DEPENDENCY_DIR, local_file.split('/')[-1]))
            LOGGER.debug(f"Transport local_file: {local_file} to remote machine {self.ip} "
                         f"remote_file: {remote_file}")

            remote_file_list.append(remote_file)
            sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")
        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", constant.DEPENDENCY_DIR, component_name + shell_file)
            sh_cmd = f"sudo bash {sh_file_remote_path}"
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
            LOGGER.error(f"Remote machine {self.ip} install {component_name} failed.")
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)
        self.__install_component_on_lkptest("CompatibilityTesting", sftp_client, ssh_client)
        self.__install_component_on_lkptest("DevkitDistribute", sftp_client, ssh_client)

    def __install_component_on_lkptest(self, sub_component_name, sftp_client, ssh_client):
        # 上传 tar.gz 文件
        LOGGER.info(f"Install component in remote machine {self.ip}: {sub_component_name}")
        remote_file_list = []
        url_and_save_path = component_collection_map.get("LkpTests").get(sub_component_name)

        local_file = url_and_save_path.get("save_path")
        remote_file = os.path.abspath(os.path.join('/tmp', constant.DEPENDENCY_DIR, local_file.split('/')[-1]))
        LOGGER.debug(f"Transport local_file: {local_file} to remote machine {self.ip} "
                     f"remote_file: {remote_file}")
        remote_file_list.append(remote_file)
        sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")
        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), sub_component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", constant.DEPENDENCY_DIR, sub_component_name + shell_file)
            sh_cmd = f"bash {sh_file_remote_path} {remote_file_list[0]}"
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
            LOGGER.info(f"Remote machine {self.ip} install {sub_component_name} success.")
        else:
            LOGGER.error(f"Remote machine {self.ip} install {sub_component_name} failed.")
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)

    def deploy_iso_handle(self, component_name, sftp_client, ssh_client):
        self._remote_exec_command(CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR, ssh_client)

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
        self._remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, ssh_client)
        self._remote_exec_command(CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE, ssh_client)

        # 上传 组件压缩包和校验文件
        LOGGER.info(f"Install component in remote machine {self.ip}: {component_name}")
        remote_file_list = []
        shell_dict = component_collection_map.get(component_name)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            local_file = url_and_save_path.get("save_path")
            remote_file = os.path.abspath(os.path.join('/tmp', local_file))
            LOGGER.debug(f"Transport local_file: {local_file} to remote machine {self.ip} "
                         f"remote_file: {remote_file}")
            remote_file_list.append(remote_file)
            sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")

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

    @timeout_decorator.timeout(100)
    def _remote_exec_command(self, cmd, ssh_client):
        try:
            stdin, stdout, stderr = ssh_client.exec_command(cmd, timeout=90)
        except (paramiko.ssh_exception.SSHException, socket.timeout) as e:
            LOGGER.error(f"Remote machine {self.ip} exec '{cmd}' failed Please run this command in this machine.")
            raise OSError(PROMPT_MAP.get(cmd, f"Remote machine {self.ip} exec '{cmd}' failed."))
        exit_status = stdout.channel.recv_exit_status()
        if exit_status == 0:
            LOGGER.debug(f"Remote machine {self.ip} exec '{cmd}' success.")
        else:
            LOGGER.error(f"Remote machine {self.ip} exec '{cmd}' failed. Please run this command in this machine.")
            raise OSError(PROMPT_MAP.get(cmd, f"Remote machine {self.ip} exec '{cmd}' failed."))

    def transport_shell_file_and_execute(self, ssh_client, sftp_client, sh_file_local_path, sh_file_remote_path,
                                         sh_cmd):
        if not os.path.exists(sh_file_local_path):
            LOGGER.error(f"{sh_file_local_path} not exists.")
            raise FileNotFoundError(f"local file {sh_file_local_path} not exists.")

        LOGGER.debug(f"Transport local_file: {sh_file_local_path} to remote machine {self.ip} "
                     f"remote_file: {sh_file_remote_path}")
        sftp_client.put(localpath=sh_file_local_path, remotepath=sh_file_remote_path)

        stdin, stdout, stderr = ssh_client.exec_command(sh_cmd)
        output = stdout.read().decode().strip()
        LOGGER.info(f"Remote machine {self.ip} '{sh_cmd}' output: {output}")
        return output

    def clear_tmp_file_at_remote_machine(self, ssh_client, remote_file_list):
        LOGGER.debug(f"Clear tmp file at remote machine {self.ip}")
        for remote_file in remote_file_list:
            try:
                remote_file = os.path.realpath(remote_file)
                if not remote_file.startswith(os.path.join("/tmp", constant.DEPENDENCY_DIR)):
                    continue
                LOGGER.debug(f"Delete tmp file at remote machine {self.ip}: {remote_file}")
                ssh_client.exec_command(f"rm -fr {remote_file}")
            except Exception as e:
                LOGGER.debug(str(e))

    def do_nothing(self, component_name, sftp_client, ssh_client):
        return

    def undeploy_iso_handle(self, component_name, sftp_client, ssh_client):
        # 需要检查本地镜像是否安装成功
        self._remote_exec_command(CHECK_MIRROR_INSTALL_STATUS, ssh_client)

        component_name = component_name.replace("Un", "")
        LOGGER.info(f"Umount component in remote machine {self.ip}: {component_name}")
        local_path = os.path.abspath(CommandLine.iso_path)
        remote_path = os.path.join("/home", local_path.split('/')[-1])

        # 上传并执行 卸载脚本
        remote_file_list = []
        for shell_file in ["uninstall.sh"]:
            sh_file_local_path = os.path.join(base_path("component"), component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", component_name + shell_file)
            sh_cmd = f"bash {sh_file_remote_path}"
            execute_output = (
                self.transport_shell_file_and_execute(
                    ssh_client, sftp_client,
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            remote_file_list.append(sh_file_remote_path)
        remote_file_list.append(remote_path)
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(ssh_client, remote_file_list)
