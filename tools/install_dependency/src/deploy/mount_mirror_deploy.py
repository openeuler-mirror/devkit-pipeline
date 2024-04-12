import os
from deploy.deploy_base import DeployBase
from utils import CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR, remote_exec_command, MKDIR_TMP_DEVKITDEPENDENCIES_CMD
from deploy.deploy_command_line import CommandLine


class MountISODeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(MountISODeploy, self).__init__(component,
                                             ip, user, pkey, password, hook_before_upload_fn=self.hook_before_upload_fn,
                                             hook_after_install_fn=None)
        self.upload_func = False
        self.install_param = True

    def hook_before_upload_fn(self):
        remote_exec_command(CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR, self.ssh_client, self.LOGGER, self.ip)
        remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)
        self.LOGGER.info(f"Deploy component in remote machine {self.ip}: {self.component}")
        local_path = os.path.abspath(CommandLine.iso_path)
        remote_path = os.path.join("/home", local_path.split('/')[-1])
        self.LOGGER.debug(f"Transport local_file: {local_path} to remote machine {self.ip} "
                     f"remote_file: {remote_path}")
        self.sftp_client.put(localpath=local_path, remotepath=remote_path)
