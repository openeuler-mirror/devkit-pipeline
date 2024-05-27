import os
import constant
from component_install.deploy_base import DeployBase


class DevkitTesterDeploy(DeployBase):
    component_name = "DevKitTester"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "devkit_tester.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "DevKitTester" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "DevKitTester" + "check_install_result.sh"),
    ]
