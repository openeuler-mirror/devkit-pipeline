import logging
import os
import socket

import paramiko
import timeout_decorator

from exception.connect_exception import (CreatePkeyFailedException, ConnectRemoteException,
                                         NotMatchedMachineTypeException)
from utils import validate_path


class DeployHelp:
    def __init__(self, ip, user, pkey, password, logger):
        self.ip = ip
        self.user = user
        self.pkey = pkey
        self.password = password
        self.LOGGER = logger

        pass

    def check_is_aarch64(self):
        machine_type = self.get_machine_type()
        self.LOGGER.info(f"{self.ip} machine type: {machine_type}")
        if machine_type != "aarch64":
            self.LOGGER.error(f"Machine type of {self.ip} is {machine_type}, not aarch64. Please replace this machine.")
            raise NotMatchedMachineTypeException()

    def get_machine_type(self):
        try:
            ssh_client = self.ssh_client()
            stdin, stdout, stderr = ssh_client.exec_command("uname -m", timeout=10)
        except (paramiko.ssh_exception.SSHException, socket.timeout) as e:
            self.LOGGER.error(f"Connect remote {self.ip} failed. {str(e)}")
            raise ConnectRemoteException()
        stdout_output = stdout.read().decode().strip()
        ssh_client.close()
        return stdout_output

    def ssh_client(self):
        ssh = paramiko.SSHClient()
        ssh._transport = self.transport_connect()
        return ssh

    def sftp_client(self):
        sftp = paramiko.SFTPClient.from_transport(self.transport_connect())
        return sftp

    def transport_connect(self):
        if not validate_path(self.pkey) or not os.path.isfile(self.pkey):
            self.LOGGER.error("Yaml file content not correct. Given pkey not exists.")
            raise ConnectRemoteException()
        try:
            # 指定本地的RSA私钥文件。如果建立密钥对时设置的有密码，password为设定的密码，如无不用指定password参数
            pkey = paramiko.RSAKey.from_private_key_file(self.pkey, password=self.password)
        except (IOError,) as e:
            self.LOGGER.error(f"Pkey file not exists. {str(e)}")
            raise CreatePkeyFailedException()
        except (paramiko.ssh_exception.PasswordRequiredException, paramiko.ssh_exception.AuthenticationException) as e:
            self.LOGGER.warning(f"Pkey password is required. {str(e)}")
            password = input(f"Press Enter to input password of {self.pkey}: ")
            self.password = password
            return self.transport_connect()
        except (paramiko.ssh_exception.SSHException,) as e:
            self.LOGGER.error(f"Connect remote {self.ip} failed because of wrong pkey. {str(e)}")
            raise CreatePkeyFailedException()

        try:
            transport = self.transport_connect_with_timeout(pkey)
        except (paramiko.ssh_exception.AuthenticationException,
                paramiko.ssh_exception.SSHException,
                timeout_decorator.TimeoutError,
                socket.gaierror,
                socket.timeout,
                socket.error) as e:
            self.LOGGER.error(f"Connect remote {self.ip} failed. {str(e)}")
            raise ConnectRemoteException()
        return transport

    @timeout_decorator.timeout(10)
    def transport_connect_with_timeout(self, pkey):
        transport = paramiko.Transport((self.ip, 22))
        transport.connect(username=self.user, pkey=pkey)
        return transport
