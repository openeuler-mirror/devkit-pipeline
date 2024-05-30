import os
import constant
from component_install.deploy_base import DeployBase


class CompatibilityDeploy(DeployBase):
    component_name = "CompatibilityTesting"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "compatibility_testing.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "CompatibilityTesting" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "CompatibilityTesting" + "check_install_result.sh"),
    ]
