import os
import sys
import yaml
from constant import ROLE_LIST, ROLE_COMPONENT

CHECK_TAR_AVAILABLE_CMD = "which tar"
MKDIR_TMP_DEVKITDEPENDENCIES_CMD = "mkdir -p /tmp/devkitdependencies"
YUM_INSTALL_LKP_DEPENDENCIES_CMD = "sudo yum install -y git wget rubygems"
CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR = "[[ $(df -m /home | awk 'NR==2' | awk '{print $4}') -gt 17*1024 ]]"
CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE = "[[ $(df -m /tmp | awk 'NR==2' | awk '{print $4}') -gt 1024 ]]"
CHECK_MIRROR_INSTALL_STATUS = "test -d /etc/yum.repos.d/yum.repos.backup"

PROMPT_MAP = {
    CHECK_TAR_AVAILABLE_CMD: "'tar' command not available.",
    MKDIR_TMP_DEVKITDEPENDENCIES_CMD: "Directory /tmp/devkitdependencies not exists.",
    YUM_INSTALL_LKP_DEPENDENCIES_CMD: "Yum install lkp dependencies failed.",
    CHECK_HOME_SPACE_SUFFICIENT_FOR_MIRROR: "Machine /home space not sufficient for mirror.",
    CHECK_TMP_SPACE_SUFFICIENT_FOR_PACKAGE: "Machine /tmp space not sufficient for package.",
    CHECK_MIRROR_INSTALL_STATUS: "Mirror mount status not correct.",
}


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
