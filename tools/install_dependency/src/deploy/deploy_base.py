import logging
import os

import timeout_decorator

import constant
from deploy.deploy_help import DeployHelp
from download.download_utils import component_collection_map
from utils import (base_path, MKDIR_TMP_DEVKITDEPENDENCIES_CMD,
                   CHECK_TAR_AVAILABLE_CMD, CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE,
                   CHECK_MIRROR_INSTALL_STATUS, remote_exec_command, SHELL_FILE_LIST, global_value)


class DeployBase:
    def __init__(self, component,
                 ip, user, pkey, password, hook_before_upload_fn=None, hook_after_install_fn=None):
        self.component = component
        self.before_upload_fn = hook_before_upload_fn
        self.after_install_fn = hook_after_install_fn
        self.LOGGER = logging.getLogger("install_dependency")
        self.ip = ip
        self.user = user
        self.pkey = pkey
        self.upload_func = True
        self.install_func = True
        self.install_param = False
        self.password = password
        self.deploy_helper = DeployHelp(ip, user, pkey, password, self.LOGGER)
        # todo 把这个用的时候在创建
        self.sftp_client = None
        self.ssh_client = None
        self.remote_file_list = []
        # 去掉这个，每个component直接以compoent去调用

    def clear_tmp_file_at_remote_machine(self, remote_file_list):
        self.LOGGER.debug(f"Clear tmp file at remote machine {self.ip}")
        for remote_file in remote_file_list:
            try:
                remote_file = os.path.realpath(remote_file)
                if not remote_file.startswith(os.path.join("/tmp", constant.DEPENDENCY_DIR)):
                    continue
                self.LOGGER.debug(f"Delete tmp file at remote machine {self.ip}: {remote_file}")
                self.ssh_client.exec_command(f"rm -fr {remote_file}")
            except Exception as e:
                self.LOGGER.debug(str(e))

    def upload(self):
        remote_exec_command(CHECK_TAR_AVAILABLE_CMD, self.ssh_client, self.LOGGER, self.ip)
        remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)
        remote_exec_command(CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE, self.ssh_client, self.LOGGER, self.ip)
        # 上传 lkp-tests.tar.gz文件
        self.LOGGER.info(f"Install component in remote machine {self.ip}: {self.component}")

        shell_dict = component_collection_map.get(self.component)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            local_file = url_and_save_path.get("save_path")
            remote_file = os.path.abspath(os.path.join('/tmp', constant.DEPENDENCY_DIR, local_file.split('/')[-1]))
            self.LOGGER.debug(f"Transport local_file: {local_file} to remote machine {self.ip} "
                              f"remote_file: {remote_file}")

            self.remote_file_list.append(remote_file)
            self.sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")

    def transport_shell_file_and_execute(self, sh_file_local_path, sh_file_remote_path,
                                         sh_cmd):
        if not os.path.exists(sh_file_local_path):
            self.LOGGER.error(f"{sh_file_local_path} not exists.")
            raise FileNotFoundError(f"local file {sh_file_local_path} not exists.")

        self.LOGGER.debug(f"Transport local_file: {sh_file_local_path} to remote machine {self.ip} "
                     f"remote_file: {sh_file_remote_path}")
        self.sftp_client.put(localpath=sh_file_local_path, remotepath=sh_file_remote_path)

        stdin, stdout, stderr = self.ssh_client.exec_command(sh_cmd)
        output = stdout.read().decode().strip()
        self.LOGGER.info(f"Remote machine {self.ip} '{sh_cmd}' output: {output}")
        return output

    def install(self):
        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), self.component, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", constant.DEPENDENCY_DIR, self.component + shell_file)
            if self.install_param:
                sh_cmd = f"bash {sh_file_remote_path} {sh_file_remote_path}"
            else:
                sh_cmd = f"bash {sh_file_remote_path}"
            execute_output = (
                self.transport_shell_file_and_execute(
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            self.remote_file_list.append(sh_file_remote_path)
            if shell_file == SHELL_FILE_LIST[1]:
                install_result = execute_output

        if install_result == "true":
            self.LOGGER.info(f"Remote machine {self.ip} install {self.component} success.")
        else:
            self.LOGGER.error(f"Remote machine {self.ip} install {self.component} failed.")

    def clean(self):
        # 清理tmp临时文件
        self.clear_tmp_file_at_remote_machine(self.remote_file_list)
        
    def install_component_handler(self):
        try:
            self.sftp_client = self.deploy_helper.sftp_client()
            self.ssh_client = self.deploy_helper.ssh_client()
            if self.before_upload_fn:
                self.before_upload_fn()
            if self.upload_func:
                self.upload()
            if self.install_func:
                self.install()
            if self.after_install_fn:
                self.after_install_fn()
            self.clean()
        except timeout_decorator.TimeoutError as e:
            self.LOGGER.error(f"Remote machine {self.ip} occur Error: Exec cmd {str(e)}")
        except (FileNotFoundError, PermissionError, NotADirectoryError, OSError, IOError) as e:
            self.LOGGER.error(f"Remote machine {self.ip} occur Error: {str(e)}")
        finally:
            self.clear_tmp_file_at_remote_machine([os.path.join("/tmp/", constant.DEPENDENCY_DIR)])
            self.ssh_client.close()
            self.sftp_client.close()
