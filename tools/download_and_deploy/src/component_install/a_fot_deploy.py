import os
import constant
from component_install.deploy_base import DeployBase
from utils import CHECK_PERF_AVAILABLE_CMD


class AFotDeploy(DeployBase):
    component_name = "A-FOT"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "a-fot.tar.gz"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "A-FOT" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "A-FOT" + "check_install_result.sh"),
    ]

    @classmethod
    def before_upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, CHECK_PERF_AVAILABLE_CMD)
