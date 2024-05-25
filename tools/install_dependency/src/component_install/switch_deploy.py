import os
import constant
from component_install.deploy_base import DeployBase
from utils import MKDIR_TMP_DEVKITDEPENDENCIES_CMD


class SwitchDeploy(DeployBase):
    component_name = "NonInvasiveSwitching"
    remote_file_list = [
        os.path.join("/tmp/", constant.DEPENDENCY_DIR, "NonInvasiveSwitching" + "install.sh"),
        os.path.join("/tmp/", constant.DEPENDENCY_DIR, "NonInvasiveSwitching" + "check_install_result.sh"),
    ]

    @classmethod
    def upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, MKDIR_TMP_DEVKITDEPENDENCIES_CMD)
