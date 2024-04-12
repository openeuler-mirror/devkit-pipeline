# todo 这个原来的逻辑写的跟咱们通用的有点不一样要改

import os
import subprocess
from download.download_utils import component_collection_map
from deploy.deploy_base import DeployBase
from utils import base_path, MKDIR_TMP_DEVKITDEPENDENCIES_CMD, remote_exec_command


class DevkitWebDeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(DevkitWebDeploy, self).__init__(component,
                 ip, user, pkey, password, hook_before_upload_fn=self.hook_before_upload_fn, hook_after_install_fn=self.hook_after_install_fn)
        self.install_func = False
        self.upload_func = False

    def hook_before_upload_fn(self):
        remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)

        self.LOGGER.info(f"Install component in remote machine {self.ip}: {self.component}")
        shell_dict = component_collection_map.get(self.component)

        url_and_save_path = shell_dict.get("download file")
        local_file = url_and_save_path.get("save_path")
        remote_file = os.path.abspath(os.path.join('/opt', local_file.split('/')[-1]))
        self.LOGGER.debug(f"Transport local_file: {local_file} to remote machine {self.ip} "
                     f"remote_file: {remote_file}")
        self.remote_file_list.append(remote_file)
        self.sftp_client.put(localpath=f"{local_file}", remotepath=f"{remote_file}")

        cmd = f"{os.path.join(base_path('component'), self.component, 'devkit_installer')} " \
              f"-i {self.ip} -u {self.user} -p {self.pkey} -paname {local_file.split('/')[-1]}"
        self.LOGGER.debug(f"Executing command: {cmd}")
        subprocess.run(cmd.split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)

    def hook_after_install_fn(self):
        local_file = component_collection_map.get(self.component).get("download file").get("save_path")
        cmd = f"{os.path.join(base_path('component'), self.component, 'devkit_installer')} " \
              f"-i {self.ip} -u {self.user} -p {self.pkey} -paname {local_file.split('/')[-1]}"
        self.LOGGER.debug(f"Executing command: {cmd}")
        subprocess.run(cmd.split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)
