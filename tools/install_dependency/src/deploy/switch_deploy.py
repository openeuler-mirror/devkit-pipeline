from deploy.deploy_base import DeployBase
from utils import remote_exec_command, MKDIR_TMP_DEVKITDEPENDENCIES_CMD


class SwitchDeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(SwitchDeploy, self).__init__(component,
                                           ip, user, pkey, password, hook_before_upload_fn=self.hook_before_upload_fn,
                                           hook_after_install_fn=None)
        self.upload_func = False

    def hook_before_upload_fn(self):
        remote_exec_command(MKDIR_TMP_DEVKITDEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)

