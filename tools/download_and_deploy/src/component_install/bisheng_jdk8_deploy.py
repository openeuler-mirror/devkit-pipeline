import os
import constant
from component_install.deploy_base import DeployBase


class BiShengJDK8Deploy(DeployBase):
    component_name = "BiShengJDK8"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "bisheng-jdk-8u402-linux-aarch64.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengJDK8" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengJDK8" + "check_install_result.sh"),
    ]
