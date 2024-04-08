import constant
import os
from deploy.deploy_base import DeployBase
from utils import (CHECK_PERF_AVAILABLE_CMD, remote_exec_command, MKDIR_TMP_DEVKITDEPENDENCIES_CMD)


class AFotDeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(AFotDeploy, self).__init__(component,
                 ip, user, pkey, password, hook_before_upload_fn=self.hook_before_upload_fn, hook_after_install_fn=None)
        self.upload_func = False

    def hook_before_upload_fn(self):
        remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)
        remote_exec_command(CHECK_PERF_AVAILABLE_CMD, self.ssh_client, self.LOGGER, self.ip)
        saved_path = os.path.join(constant.DEFAULT_PATH, "a-fot.tar.gz")
        remote_file = os.path.abspath(os.path.join('/tmp', saved_path))
        self.LOGGER.debug(f"Transport local_file: {saved_path} to remote machine {self.ip} "
                     f"remote_file: {remote_file}")
        self.sftp_client.put(localpath=f"{saved_path}", remotepath=f"{remote_file}")


