import os
import constant
from component_install.deploy_base import DeployBase


class BiShengJDK17Deploy(DeployBase):
    component_name = "BiShengJDK17"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "bisheng-jdk-17.0.10-linux-aarch64.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengJDK17" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengJDK17" + "check_install_result.sh"),
    ]
