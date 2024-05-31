import os
import timeout_decorator

import constant
from component_install.deploy_base import DeployBase, LOGGER
from utils import PROMPT_MAP, MKDIR_TMP_DEVKITDEPENDENCIES_CMD


class ClamAVDeploy(DeployBase):
    component_name = "ClamAV"
    remote_file_list = [
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "ClamAV" + "install.sh"),
        os.path.join("/tmp", constant.DEPENDENCY_DIR, "ClamAV" + "check_install_result.sh"),
    ]

    @classmethod
    def upload(cls, machine, sftp_client, ssh_client):
        cls._remote_exec_command(machine.ip, ssh_client, MKDIR_TMP_DEVKITDEPENDENCIES_CMD)

        cmd = "yum install clamav -y"
        ip = machine.ip
        try:
            cls._remote_exec_command(ip, ssh_client, cmd)
        except (timeout_decorator.TimeoutError, OSError, IOError) as e:
            LOGGER.error(f"Remote machine {ip} install {cls.component_name} failed. "
                         f"Exec '{cmd}' failed Please run this command in this machine.")
            raise OSError(PROMPT_MAP.get(cmd, f"Remote machine {ip} exec '{cmd}' failed."))
