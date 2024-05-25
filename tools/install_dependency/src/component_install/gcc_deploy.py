import os
import constant
from component_install.deploy_base import DeployBase


class GccDeploy(DeployBase):
    component_name = "GCCforOpenEuler"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "gcc-10.3.1-2023.12-aarch64-linux.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "GCCforOpenEuler" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "GCCforOpenEuler" + "check_install_result.sh"),
    ]
