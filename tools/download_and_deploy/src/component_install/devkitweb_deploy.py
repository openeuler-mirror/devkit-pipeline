import os
import subprocess

import constant
from download.download_utils import component_collection_map
from component_install.deploy_base import DeployBase, LOGGER
from utils import base_path, CHECK_OPT_SPACE_SUFFICIENT_FOR_PACKAGE, CHECK_SUDO_PERMISSION


class DevkitWebDeploy(DeployBase):
    component_name = "DevKitWeb"
    remote_file_list = [
        os.path.join("/opt", "DevKit-All-24.0.RC1-Linux-Kunpeng.tar.gz")
    ]

    @classmethod
    def before_upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_SUDO_PERMISSION)
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_OPT_SPACE_SUFFICIENT_FOR_PACKAGE)

    @classmethod
    def install(cls, machine, sftp_client, ssh_client):
        origin_local_file = component_collection_map.get(cls.component_name).get("save_path")
        package_name = origin_local_file.split('/')[-1]

        remote_file = os.path.abspath(os.path.join('/tmp', constant.DEPENDENCY_DIR, package_name))
        final_path = os.path.abspath(os.path.join('/opt', package_name))
        cls._remote_exec_command(machine.ip, ssh_client, f"sudo /bin/mv -f {remote_file} {final_path}")

        cmd = f"{os.path.join(base_path('component'), cls.component_name, 'devkit_installer')} " \
              f"-i {machine.ip} -u {machine.user} -p {machine.pkey} -paname {package_name} --debug"
        LOGGER.debug(f"Executing command: {cmd}")
        result = subprocess.run(cmd.split(' '),
                                capture_output=False, shell=False, stderr=subprocess.STDOUT)
        if result.returncode == 0:
            machine.component_dict[cls.component_name] = "install success."
