import os

from deploy.deploy_base import DeployBase
from utils import CHECK_MIRROR_INSTALL_STATUS, base_path, remote_exec_command, MKDIR_TMP_DEVKITDEPENDENCIES_CMD
from deploy.deploy_command_line import CommandLine


class UnMountISODeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(UnMountISODeploy, self).__init__(component,
                                               ip, user, pkey, password,
                                               hook_before_upload_fn=self.hook_before_upload_fn,
                                               hook_after_install_fn=self.hook_after_install_fn)
        self.install_func = False
        self.upload_func = False

    def hook_before_upload_fn(self):
        remote_exec_command(CHECK_MIRROR_INSTALL_STATUS, self.ssh_client, self.LOGGER, self.ip)
        self.component = self.component.replace("Un", "")

    def hook_after_install_fn(self):
        remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)
        local_path = os.path.abspath(CommandLine.iso_path)
        remote_path = os.path.join("/home", local_path.split('/')[-1])
        for shell_file in ["uninstall.sh"]:
            sh_file_local_path = os.path.join(base_path("component"), self.component, shell_file)
            sh_file_remote_path = os.path.join("/tmp/", self.component + shell_file)
            sh_cmd = f"bash {sh_file_remote_path}"
            execute_output = (
                self.transport_shell_file_and_execute(
                    sh_file_local_path=sh_file_local_path,
                    sh_file_remote_path=sh_file_remote_path,
                    sh_cmd=sh_cmd
                ))
            self.remote_file_list.append(sh_file_remote_path)
        self.remote_file_list.append(remote_path)
