
from deploy.deploy_base import DeployBase


class CompatibilityDeploy(DeployBase):
    def __init__(self, component,
                 ip, user, pkey, password):
        super(CompatibilityDeploy, self).__init__(component,
                 ip, user, pkey, password, hook_before_upload_fn=None, hook_after_install_fn=None)
        self.sudo = True
