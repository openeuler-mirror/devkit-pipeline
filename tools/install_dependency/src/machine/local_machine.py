import os
import logging
import subprocess

import constant
from command_line import CommandLine
from exception.connect_exception import NotMatchedMachineTypeException
from download import component_collection_map
from utils import base_path

LOGGER = logging.getLogger("install_dependency")
SHELL_FILE_LIST = ["install.sh", "check_install_result.sh"]


class LocalMachine:
    def __init__(self, ip):
        self.ip = ip
        self.check_is_aarch64()

    def check_is_aarch64(self):
        machine_type = os.uname().machine.lower()
        LOGGER.info(f"{self.ip} machine type: {machine_type}")
        if machine_type != "aarch64":
            LOGGER.error(f"Machine type of {self.ip} is {machine_type}, not aarch64. Please replace this machine.")
            raise NotMatchedMachineTypeException()

    def install_component(self, component_name):
        try:
            self.install_component_handler(component_name)
        except (FileNotFoundError, PermissionError, NotADirectoryError, OSError, IOError) as e:
            LOGGER.error(f"Local machine {self.ip} occur Error: {str(e)}")

    def install_component_handler(self, component_name):
        component_name_to_func_dict = {
            "GCCforOpenEuler": self.default_install_component_handle,
            "BiShengCompiler": self.default_install_component_handle,
            "BiShengJDK17": self.default_install_component_handle,
            "BiShengJDK8": self.default_install_component_handle,
            "OpenEulerMirrorISO": self.deploy_iso_handle,
        }
        return component_name_to_func_dict.get(component_name)(component_name)

    def deploy_iso_handle(self, component_name):
        # 复制镜像文件
        LOGGER.info(f"Deploy component in local machine {self.ip}: {component_name}")
        local_path = os.path.abspath(CommandLine.iso_path)

        # 执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), component_name, shell_file)
            sh_cmd = f"bash {sh_file_local_path} {local_path}"
            if not os.path.exists(sh_file_local_path):
                LOGGER.error(f"{sh_file_local_path} not exists.")
                raise FileNotFoundError(f"local file {sh_file_local_path} not exists.")

            result = subprocess.run(f"{sh_cmd}".split(' '),
                                    capture_output=True, shell=False)
            output = result.stdout.decode().strip()
            LOGGER.info(f"Local machine {self.ip} '{sh_cmd}' file output: {output}")
            if shell_file == SHELL_FILE_LIST[1]:
                install_result = output

        if install_result == "true":
            LOGGER.info(f"Remote machine {self.ip} deploy {component_name} success.")
        else:
            LOGGER.info(f"Remote machine {self.ip} deploy {component_name} failed.")

    def default_install_component_handle(self, component_name):
        result = subprocess.run(f"mkdir -p /tmp/{constant.DEPENDENCY_DIR}".split(' '),
                                capture_output=False, shell=False, stderr=subprocess.STDOUT)
        if result.returncode == 0:
            LOGGER.debug(f"Local machine {self.ip} mkdir -p /tmp/{constant.DEPENDENCY_DIR} result: success")
        else:
            LOGGER.error(f"Local machine {self.ip} mkdir -p /tmp/{constant.DEPENDENCY_DIR} result: failed")
            raise NotADirectoryError(f"Local machine {self.ip} "
                                     f"directory {os.path.join('/tmp/', constant.DEPENDENCY_DIR)} not exist.")

        # 上传 组件压缩包和校验文件
        LOGGER.info(f"Install component in local machine {self.ip}: {component_name}")
        remote_file_list = []
        shell_dict = component_collection_map.get(component_name)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            component = url_and_save_path.get("save_path")
            LOGGER.debug(f"Copy component file to local machine {self.ip}: {component}")
            remote_file = os.path.abspath(os.path.join('/tmp', component))
            remote_file_list.append(remote_file)
            subprocess.run(f"/bin/cp -f {component} {remote_file}".split(' '),
                           capture_output=False, shell=False, stderr=subprocess.STDOUT)

        # 上传并执行 安装脚本, 校验安装结果脚本
        install_result = ""
        for shell_file in SHELL_FILE_LIST:
            sh_file_local_path = os.path.join(base_path("component"), component_name, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", constant.DEPENDENCY_DIR, component_name + shell_file)
            sh_cmd = f"bash {sh_file_remote_path}"
            execute_output = (
                self.transport_shell_file_and_execute(
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            remote_file_list.append(sh_file_remote_path)
            if shell_file == SHELL_FILE_LIST[1]:
                install_result = execute_output

        if install_result == "true":
            LOGGER.info(f"Local machine {self.ip} install {component_name} success.")
        else:
            LOGGER.error(f"Local machine {self.ip} install {component_name} failed.")
        # 清理tmp临时文件
        self.clear_tmp_file_at_local_machine(remote_file_list)

    def transport_shell_file_and_execute(self, sh_file_local_path, sh_file_remote_path, sh_cmd):
        if not os.path.exists(sh_file_local_path):
            LOGGER.error(f"{sh_file_local_path} not exists.")
            raise FileNotFoundError(f"local file {sh_file_local_path} not exists.")

        LOGGER.debug(f"Copy local_file: {sh_file_local_path} to local machine {self.ip} "
                     f"remote_file: {sh_file_remote_path}")
        subprocess.run(f"/bin/cp -f {sh_file_local_path} {sh_file_remote_path}".split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)

        result = subprocess.run(f"{sh_cmd}".split(' '),
                                capture_output=True, shell=False)
        output = result.stdout.decode().strip()
        LOGGER.info(f"Local machine {self.ip} '{sh_cmd}' file output: {output}")
        return output

    def clear_tmp_file_at_local_machine(self, remote_file_list):
        LOGGER.debug(f"Clear tmp file at local machine {self.ip}")
        for remote_file in remote_file_list:
            LOGGER.debug(f"Delete tmp file at local machine {self.ip}: {remote_file}")
            subprocess.run(f"rm -f {remote_file}".split(' '),
                           capture_output=False, shell=False, stderr=subprocess.STDOUT)
