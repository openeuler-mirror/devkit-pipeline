import logging
import os
import socket
import time
import timeout_decorator

import paramiko

LOGGER = logging.getLogger("devkit_installer")


class DevKitMachine:
    def __init__(self, ip, user, pkey):
        self.ip = ip
        self.user = user
        self.pkey = pkey
        self.install_path = "/opt"
        self.install_dir = ""
        self.install_file = "install.sh"

    def ssh_client(self):
        try:
            ssh = paramiko.SSHClient()
            ssh._transport = self.transport_connect(self.user, self.pkey)
        except (OSError, IOError, Exception) as e:
            LOGGER.error(f"Unable to connect to {self.ip}. Error occurs: {str(e)}")
            return None
        return ssh

    def transport_connect(self, user, pkey_path, password=None):
        try:
            # 指定本地的RSA私钥文件。如果建立密钥对时设置的有密码，password为设定的密码，如无不用指定password参数
            pkey = paramiko.RSAKey.from_private_key_file(pkey_path, password=password)
        except (IOError,) as e:
            LOGGER.error(f"Pkey file not exists. {str(e)}")
            raise IOError()
        except (paramiko.ssh_exception.PasswordRequiredException, paramiko.ssh_exception.AuthenticationException) as e:
            LOGGER.warning(f"Pkey password is required. {str(e)}")
            raise IOError(str(e))
        except (paramiko.ssh_exception.SSHException,) as e:
            LOGGER.error(f"Connect remote {self.ip} failed because of wrong pkey. {str(e)}")
            raise IOError()

        try:
            transport = self.transport_connect_with_timeout(user, pkey)
        except (paramiko.ssh_exception.AuthenticationException,
                paramiko.ssh_exception.SSHException,
                timeout_decorator.TimeoutError,
                socket.gaierror,
                socket.timeout,
                socket.error) as e:
            LOGGER.error(f"Connect remote {self.ip} failed. {str(e)}")
            raise IOError()
        return transport

    @timeout_decorator.timeout(10)
    def transport_connect_with_timeout(self, user, pkey):
        transport = paramiko.Transport((self.ip, 22))
        transport.connect(username=user, pkey=pkey)
        return transport

    @staticmethod
    def channel_create(ssh_client):
        chan = ssh_client.invoke_shell()
        time.sleep(3)
        chan.recv(65535)
        return chan

    def decompress_package(self, package_path, package_name):
        ssh_client = self.ssh_client()
        if not ssh_client:
            return False
        ret = self.decompress_package_handle(ssh_client, package_path, package_name)
        ssh_client.close()
        return ret

    def decompress_package_handle(self, ssh_client, package_dir, package_name):
        package_file = os.path.join(package_dir, package_name)
        stdin, stdout, stderr = ssh_client.exec_command(f"ls {package_file}")
        stdout_output = stdout.read().decode().strip()
        if stdout_output != package_file:
            LOGGER.error(f"DevKit install package not exists: {package_file}")
            return False

        cmd = f"cd {package_dir} && sudo tar -zxf {package_name}"
        stdin, stdout, stderr = ssh_client.exec_command(cmd)
        exit_status = stdout.channel.recv_exit_status()
        if exit_status == 0:
            LOGGER.info(f"Remote machine {self.ip} exec '{cmd}' success.")
        else:
            LOGGER.error(f"Remote machine {self.ip} exec '{cmd}' failed.")
            return False

        package_name_trim = package_name.replace(".tar.gz", "")
        install_abs_path = os.path.join(package_dir, package_name_trim, "install.sh")
        stdin, stdout, stderr = ssh_client.exec_command(f"ls {install_abs_path}")
        stdout_output = stdout.read().decode().strip()
        if stdout_output != install_abs_path:
            LOGGER.error(f"DevKit install file not exists: {install_abs_path}")
            return False

        self.install_dir = str(os.path.join(package_dir, package_name_trim))
        LOGGER.info(f"DevKit install_dir: {self.install_dir}")
        return True

    def devkit_install_by_cmd(self, server_ip, server_port="8086", http_port="8002", install_path="/opt",
                              plugin="java_perf", rpc_port=50051, all_plugins=False):
        if all_plugins:
            plugin_param = "-a"
        else:
            plugin_param = f"--plugin={plugin}"

        cmd = (f"cd {self.install_dir} && sudo bash {self.install_file} {plugin_param} -d={install_path} "
               f"--ip={server_ip} --map_ip={server_ip} -p={server_port} --http_port={http_port} "
               f"--rpc_ip={server_ip} --rpc_port={rpc_port} --normal-install=1")
        LOGGER.info(f"cmd: {cmd}")

        special_end = f"https://{server_ip}:{server_port}"
        environment_check = ("Do you want to "
                             "authorize the tool to handle the items failed in the installation environment check")
        continue_statement = "Do you want to continue?"
        use_statement = "do you want to use it?"
        error_special_end = "you want to"

        ssh_client = self.ssh_client()
        if not ssh_client:
            return False
        channel = DevKitMachine.channel_create(ssh_client)
        result = self.channel_send_cmd(channel=channel, cmd=cmd, special_end=special_end,
                                       error_special_end=error_special_end, timeout=300)

        while result.find(error_special_end) != -1:
            # 授权工具安装继续运行
            result = self.channel_send_cmd(channel=channel, cmd="y\n", special_end=special_end,
                                           error_special_end=error_special_end, timeout=300)
        if result.find(special_end) != -1:
            ssh_client.close()
            return True
        ssh_client.close()
        return False

    def channel_send_cmd(self, channel, cmd, special_end, error_special_end, timeout=60):
        buff_decode = ""
        try:
            channel.settimeout(timeout)
            if not cmd.endswith('\n'):
                cmd = cmd + '\n'
            channel.send(cmd)

            while True:
                buff = channel.recv(65535)
                buff_decode = str(buff.decode("utf8", errors='ignore'))
                LOGGER.debug(buff_decode.replace("\n", ""))
                if buff_decode.find(special_end) != -1:
                    break
                if buff_decode.find(error_special_end) != -1:
                    break
            LOGGER.debug(f"process_result: {buff_decode}")

        except Exception as e:
            LOGGER.info(f"Exec '{cmd}' error occurs: {str(e)}")
        return buff_decode
