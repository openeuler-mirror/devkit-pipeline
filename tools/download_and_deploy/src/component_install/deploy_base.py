import logging
import os
import socket
import paramiko
import timeout_decorator

import constant

from download.download_utils import component_collection_map
from utils import (base_path, MKDIR_TMP_DEVKITDEPENDENCIES_CMD,
                   CHECK_TAR_AVAILABLE_CMD, CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE,
                   PROMPT_MAP)

LOGGER = logging.getLogger("deploy_tool")


class DeployBase:
    component_name = "component_name"
    sudo = False
    remote_file_list = []

    @classmethod
    def install_handle(cls, machine, sftp_client, ssh_client):
        cls.before_upload(machine, sftp_client, ssh_client)
        cls.upload(machine, sftp_client, ssh_client)
        cls.install(machine, sftp_client, ssh_client)
        cls.after_install(machine, sftp_client, ssh_client)

    @classmethod
    def before_upload(cls, machine, sftp_client, ssh_client):
        pass

    @classmethod
    def upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_TAR_AVAILABLE_CMD)
        cls._remote_exec_command(machine.ip, ssh_client, MKDIR_TMP_DEVKITDEPENDENCIES_CMD)
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE)

        # 上传 .tar.gz 文件
        shell_dict = component_collection_map.get(cls.component_name)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            local_file = url_and_save_path.get("save_path")
            remote_file = os.path.abspath(os.path.join('/tmp', constant.DEPENDENCY_DIR, local_file.split('/')[-1]))
            LOGGER.debug(f"Transport local_file: {local_file} to remote machine {machine.ip} "
                         f"remote_file: {remote_file}")

            # cls.remote_file_list.append(remote_file)
            sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")

    @classmethod
    def install(cls, machine, sftp_client, ssh_client):
        shell_file_list = ["install.sh", "check_install_result.sh"]

        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in shell_file_list:
            sh_file_local_path = os.path.join(base_path("component"), cls.component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", constant.DEPENDENCY_DIR, cls.component_name + shell_file)

            if cls.sudo:
                sh_cmd = f"sudo bash {sh_file_remote_path}"
            else:
                sh_cmd = f"bash {sh_file_remote_path}"

            execute_output = (
                cls._transport_shell_file_and_execute(
                    ip=machine.ip,
                    sftp_client=sftp_client,
                    ssh_client=ssh_client,
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            # cls.remote_file_list.append(sh_file_remote_path)
            if shell_file == shell_file_list[1]:
                install_result = execute_output

        if install_result == "true":
            LOGGER.info(f"Remote machine {machine.ip} install {cls.component_name} success.")
            machine.component_dict[cls.component_name] = "install success."
        else:
            LOGGER.error(f"Remote machine {machine.ip} install {cls.component_name} failed.")
            machine.component_dict[cls.component_name] = "install failed."

    @classmethod
    def after_install(cls, machine, sftp_client, ssh_client):
        cls._clear_tmp_file_at_remote_machine(machine.ip, ssh_client, cls.remote_file_list)

    @classmethod
    @timeout_decorator.timeout(300)
    def _remote_exec_command(cls, ip, ssh_client, cmd):
        try:
            stdin, stdout, stderr = ssh_client.exec_command(cmd, timeout=299)
        except (paramiko.ssh_exception.SSHException, socket.timeout) as e:
            LOGGER.error(f"Remote machine {ip} exec '{cmd}' failed Please run this command in this machine.")
            raise OSError(PROMPT_MAP.get(cmd, f"Remote machine {ip} exec '{cmd}' failed."))
        exit_status = stdout.channel.recv_exit_status()
        if exit_status == 0:
            LOGGER.debug(f"Remote machine {ip} exec '{cmd}' success.")
        else:
            LOGGER.error(f"Remote machine {ip} exec '{cmd}' failed. Please run this command in this machine.")
            raise OSError(PROMPT_MAP.get(cmd, f"Remote machine {ip} exec '{cmd}' failed."))

    @classmethod
    def _transport_shell_file_and_execute(cls, ip, sftp_client, ssh_client,
                                          sh_file_local_path, sh_file_remote_path,
                                          sh_cmd):
        if not os.path.exists(sh_file_local_path):
            LOGGER.error(f"{sh_file_local_path} not exists.")
            raise FileNotFoundError(f"local file {sh_file_local_path} not exists.")

        LOGGER.debug(f"Transport local_file: {sh_file_local_path} to remote machine {ip} "
                     f"remote_file: {sh_file_remote_path}")
        sftp_client.put(localpath=sh_file_local_path, remotepath=sh_file_remote_path)

        try:
            stdin, stdout, stderr = ssh_client.exec_command(sh_cmd)
        except (paramiko.ssh_exception.SSHException, socket.timeout) as e:
            LOGGER.error(f"Remote machine {ip} exec '{sh_cmd}' failed. {str(e)}")
            raise OSError(f"Remote machine {ip} exec '{sh_cmd}' failed. {str(e)}")
        output = stdout.read().decode().strip()
        LOGGER.info(f"Remote machine {ip} '{sh_cmd}' output: {output}")
        return output

    @classmethod
    def _clear_tmp_file_at_remote_machine(cls, ip, ssh_client, remote_file_list):
        LOGGER.debug(f"Clear tmp file at remote machine {ip}")
        for remote_file in remote_file_list:
            try:
                remote_file = os.path.realpath(remote_file)
                if not remote_file.startswith(os.path.join("/tmp", constant.DEPENDENCY_DIR)):
                    continue
                LOGGER.debug(f"Delete tmp file at remote machine {ip}: {remote_file}")
                ssh_client.exec_command(f"rm -fr {remote_file}")
            except Exception as e:
                LOGGER.debug(str(e))
