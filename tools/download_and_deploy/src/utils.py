import os
import sys
import yaml
import logging
from constant import ROLE_LIST, ROLE_COMPONENT

CHECK_TAR_AVAILABLE_CMD = "which tar"
CHECK_PERF_AVAILABLE_CMD = "which perf"
MKDIR_TMP_DEVKITDEPENDENCIES_CMD = "mkdir -p /tmp/devkitdependencies"
CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR = "[[ $(df -m /home | awk 'NR==2' | awk '{print $4}') -gt 17*1024 ]]"
CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE = "[[ $(df -m /tmp | awk 'NR==2' | awk '{print $4}') -gt 1024 ]]"
CHECK_OPT_SPACE_SUFFICIENT_FOR_PACKAGE = "[[ $(df -m /opt | awk 'NR==2' | awk '{print $4}') -gt 2048 ]]"
CHECK_SUDO_PERMISSION = "sudo -v &>/dev/null"
CHECK_MIRROR_INSTALL_STATUS = "test -d /etc/yum.repos.d/yum.repos.backup"


PROMPT_MAP = {
    CHECK_TAR_AVAILABLE_CMD: "'tar' command not available, \n"
                             " please execute 'yum install tar -y' in remote machine.",
    CHECK_PERF_AVAILABLE_CMD: "'perf' command not available, \n"
                              " please execute 'yum install perf -y' in remote machine.",
    MKDIR_TMP_DEVKITDEPENDENCIES_CMD: "Directory /tmp/devkitdependencies not exists.",
    CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR: "Machine /home space not sufficient for mirror.",
    CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE: "Machine /tmp space not sufficient for package.",
    CHECK_OPT_SPACE_SUFFICIENT_FOR_PACKAGE: "Machine /opt space not sufficient for package.",
    CHECK_SUDO_PERMISSION: "Machine sudo permission not configure for current user, please consider \n"
                           " 1. add sudo permission to current user.\n"
                           " 2. add sudo password-free to current user.\n",
    CHECK_MIRROR_INSTALL_STATUS: "Mirror mount status not correct.",
}

LOGGER = logging.getLogger("deploy_tool")


def validate_path(path: str) -> bool:
    return path.startswith('/') and path.find('../') == -1 and path.find('./') == -1


def base_path(path):
    if getattr(sys, 'frozen', False):
        base_dir = sys._MEIPASS
    else:
        base_dir = os.path.dirname(__file__)
    return os.path.join(base_dir, path)


def read_yaml_file(yaml_path):
    try:
        with open(yaml_path, "r") as file:
            yaml_dict = yaml.safe_load(file)
    except (FileNotFoundError, IsADirectoryError) as e:
        print(f"[ERROR] Yaml file is not in specified path. Error: {str(e)}")
        sys.exit(1)
    except (yaml.parser.ParserError,
            yaml.scanner.ScannerError,
            yaml.composer.ComposerError,
            yaml.constructor.ConstructorError) as e:
        print(f"[ERROR] Incorrect yaml file. Error: {str(e)}")
        sys.exit(1)
    return yaml_dict


def generate_component_list(yaml_dict):
    component_list = list()
    for role in ROLE_LIST:
        if role not in yaml_dict:
            continue
        component_list.extend(ROLE_COMPONENT[role])
    return list(set(component_list))
