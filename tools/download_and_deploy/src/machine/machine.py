import logging
import os
import socket

import paramiko
import timeout_decorator

import constant
from component_install.deploy_base import DeployBase
from component_install.gcc_deploy import GccDeploy
from component_install.bisheng_compiler_deploy import BiShengCompilerDeploy
from component_install.bisheng_jdk8_deploy import BiShengJDK8Deploy
from component_install.bisheng_jdk17_deploy import BiShengJDK17Deploy
from component_install.compatibility_test_deploy import CompatibilityDeploy
from component_install.devkitweb_deploy import DevkitWebDeploy
from component_install.devkitcli_deploy import DevkitCLIDeploy
from component_install.devkittester_deploy import DevkitTesterDeploy
from component_install.switch_deploy import SwitchDeploy
from component_install.a_fot_deploy import AFotDeploy
from component_install.clamav_deploy import ClamAVDeploy

from exception.connect_exception import (CreatePkeyFailedException, ConnectRemoteException,
                                         NotMatchedMachineTypeException)

from utils import validate_path, CHECK_TAR_AVAILABLE_CMD, MKDIR_TMP_DEVKITDEPENDENCIES_CMD

LOGGER = logging.getLogger("deploy_tool")


class Machine:
    def __init__(self, ip, user, pkey, password=None):
        self.ip = ip
        self.user = user
        self.pkey = pkey
        self.password = password
        self.check_is_aarch64()
        self.component_dict = dict()

    def add_component(self, components):
        for component in components:
            self.component_dict[component] = []

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

    def install_components(self):
        ssh_client = self.ssh_client()
        DeployBase._remote_exec_command(self.ip, ssh_client, CHECK_TAR_AVAILABLE_CMD)
        DeployBase._remote_exec_command(self.ip, ssh_client, MKDIR_TMP_DEVKITDEPENDENCIES_CMD)

        for component in self.component_dict:
            self.install_component(component)
        DeployBase._clear_tmp_file_at_remote_machine(
            self.ip, ssh_client, [os.path.join("/tmp/", constant.DEPENDENCY_DIR)])

    def install_component(self, component_name):
        ssh_client = self.ssh_client()
        sftp_client = self.sftp_client()
        try:
            self.install_component_handler(component_name, sftp_client, ssh_client)
        except (timeout_decorator.TimeoutError, FileNotFoundError, PermissionError, NotADirectoryError,
                OSError, IOError) as e:
            LOGGER.error(f"Remote machine {self.ip} install {component_name} occur Error: {str(e)}")
            self.component_dict[component_name] = f"install failed. Error: {str(e)}"
        finally:
            ssh_client.close()
            sftp_client.close()

    def install_component_handler(self, component_name, sftp_client, ssh_client):
        component_name_to_func_dict = {
            "GCCforOpenEuler": GccDeploy.install_handle,
            "BiShengCompiler": BiShengCompilerDeploy.install_handle,
            "BiShengJDK17": BiShengJDK17Deploy.install_handle,
            "BiShengJDK8": BiShengJDK8Deploy.install_handle,
            "CompatibilityTesting": CompatibilityDeploy.install_handle,
            "DevKitWeb": DevkitWebDeploy.install_handle,
            "DevKitCLI": DevkitCLIDeploy.install_handle,
            "DevKitTester": DevkitTesterDeploy.install_handle,
            "NonInvasiveSwitching": SwitchDeploy.install_handle,
            "A-FOT": AFotDeploy.install_handle,
            "ClamAV": ClamAVDeploy.install_handle,
        }
        return component_name_to_func_dict.get(component_name)(self, sftp_client, ssh_client)
