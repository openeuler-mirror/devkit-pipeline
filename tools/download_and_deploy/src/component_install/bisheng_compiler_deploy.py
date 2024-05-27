import os
import constant
from component_install.deploy_base import DeployBase


class BiShengCompilerDeploy(DeployBase):
    component_name = "BiShengCompiler"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengCompiler-3.2.0-aarch64-linux.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengCompiler" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "BiShengCompiler" + "check_install_result.sh"),
    ]
