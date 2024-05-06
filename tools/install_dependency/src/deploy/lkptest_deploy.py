
from deploy.deploy_base import DeployBase
from utils import YUM_INSTALL_LKP_DEPENDENCIES_CMD, remote_exec_command


class LkpTestDeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(LkpTestDeploy, self).__init__(component,
                 ip, user, pkey, password, hook_before_upload_fn=self.hook_before_upload_fn, hook_after_install_fn=None)
        self.sudo = True

    def hook_before_upload_fn(self):
        remote_exec_command(YUM_INSTALL_LKP_DEPENDENCIES_CMD, self.ssh_client, self.LOGGER, self.ip)
