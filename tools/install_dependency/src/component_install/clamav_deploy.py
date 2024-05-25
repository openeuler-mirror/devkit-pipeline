import os
import constant
from component_install.deploy_base import DeployBase


class ClamAVDeploy(DeployBase):
    component_name = "ClamAV"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "ClamAV" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "ClamAV" + "check_install_result.sh"),
    ]

    @classmethod
    def upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, "yum install clamav -y")
