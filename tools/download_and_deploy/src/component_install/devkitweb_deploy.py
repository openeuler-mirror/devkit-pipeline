import os
import subprocess

from download.download_utils import component_collection_map
from component_install.deploy_base import DeployBase, LOGGER
from utils import base_path, CHECK_OPT_SPACE_SUFFICIENT_FOR_PACKAGE, CHECK_SUDO_PERMISSION, CHECK_OPT_WRITE_PERMISSION


class DevkitWebDeploy(DeployBase):
    component_name = "DevKitWeb"
    remote_file_list = [
        os.path.join("/opt", "DevKit-All-24.0.RC1-Linux-Kunpeng.tar.gz")
    ]

    @classmethod
    def before_upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_SUDO_PERMISSION)
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_OPT_WRITE_PERMISSION)
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_OPT_SPACE_SUFFICIENT_FOR_PACKAGE)

    @classmethod
    def upload(cls, machine, sftp_client, ssh_client):
        url_and_save_path = component_collection_map.get(cls.component_name)
        local_file = url_and_save_path.get("save_path")
        remote_file = os.path.abspath(os.path.join('/opt', local_file.split('/')[-1]))
        LOGGER.debug(f"Transport local_file: {local_file} to remote machine {machine.ip} "
                     f"remote_file: {remote_file}")

        # cls.remote_file_list.append(remote_file)
        try:
            sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")
        except Exception as e:
            devkitweb_prompt = f"Remote machine copy {cls.component_name} failed. " \
                               f"Please check /opt write permission and /opt/DevKit-All-24.0.RC1-Linux-Kunpeng.tar.gz exists."
            LOGGER.error(devkitweb_prompt)
            raise IOError(devkitweb_prompt)

    @classmethod
    def install(cls, machine, sftp_client, ssh_client):
        local_file = component_collection_map.get(cls.component_name).get("save_path")
        cmd = f"{os.path.join(base_path('component'), cls.component_name, 'devkit_installer')} " \
              f"-i {machine.ip} -u {machine.user} -p {machine.pkey} -paname {local_file.split('/')[-1]} --debug"
        LOGGER.debug(f"Executing command: {cmd}")
        result = subprocess.run(cmd.split(' '),
                                capture_output=False, shell=False, stderr=subprocess.STDOUT)
        if result.returncode == 0:
            machine.component_dict[cls.component_name] = "install success."