import os
import constant
from component_install.deploy_base import DeployBase


class DevkitCLIDeploy(DeployBase):
    component_name = "DevKitCLI"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "DevKit-CLI-24.0.T50-Linux-Kunpeng.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "DevKitCLI" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "DevKitCLI" + "check_install_result.sh"),
    ]
