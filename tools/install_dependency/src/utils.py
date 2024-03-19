import os
import sys

MKDIR_TMP_DEVKITDEPENDENCIES_CMD = "mkdir -p /tmp/devkitdependencies"
YUM_INSTALL_LKP_DEPENDENCIES_CMD = "yum install -y git wget rubygems"
CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR = "[[ $(df -m /home | awk 'NR==2' | awk '{print $4}') -gt 17*1024 ]]"
CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE = "[[ $(df -m /tmp | awk 'NR==2' | awk '{print $4}') -gt 1024 ]]"
CHECK_MIRROR_INSTALL_STATUS = "test -d /etc/yum.repos.d/yum.repos.backup"

PROMPT_MAP = {
    MKDIR_TMP_DEVKITDEPENDENCIES_CMD: "Directory /tmp/devkitdependencies not exists.",
    YUM_INSTALL_LKP_DEPENDENCIES_CMD: "Yum install dependencies failed.",
    CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR: "Machine /home space not sufficient for mirror.",
    CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE: "Machine /tmp space not sufficient for package.",
    CHECK_MIRROR_INSTALL_STATUS: "Mirror mount status not correct."
}


def base_path(path):
    if getattr(sys, 'frozen', False):
        base_dir = sys._MEIPASS
    else:
        base_dir = os.path.dirname(__file__)
    return os.path.join(base_dir, path)


def available_role(role_list, data):
    list_after_verification = []
    for role in role_list:
        if role in data:
            list_after_verification.append(role)
    return list_after_verification
