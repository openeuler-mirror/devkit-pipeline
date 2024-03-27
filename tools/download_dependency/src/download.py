import os
import platform
import subprocess
import sys
import shutil
import tarfile
import wget
import yaml
import download_config
from download_utils import download_dependence_handler, download_dependence_file
from download_command_line import process_command_line, CommandLine

FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
DEFAULT_PATH = "./devkitdependencies"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"

# A-FOT files
BASE_URL = "https://gitee.com/openeuler/A-FOT/raw/master/{}"
A_FOT = "a-fot"
A_FOT_INI = "a-fot.ini"
AUTO_FDO_SH = "auto_fdo.sh"
AUTO_BOLT_SH = "auto_bolt.sh"
AUTO_PREFETCH = "auto_prefetch.sh"
SPLIT_JSON_PY = "split_json.py"
FILE_LIST = (A_FOT, A_FOT_INI, AUTO_FDO_SH, AUTO_BOLT_SH, AUTO_PREFETCH, SPLIT_JSON_PY)


component_collection_map = {
    component.get("component_name"): {
        "download file":
            {
                URL: f"{component.get(FILE)}",
                SAVE_PATH: f"{os.path.join(DEFAULT_PATH, component.get(FILE).split('/')[-1])}"
            },
        "download sha256":
            {
                URL: f"{component.get(SHA256)}",
                SAVE_PATH: f"{os.path.join(DEFAULT_PATH, component.get(SHA256).split('/')[-1])}"
            }
    } for component in (
        download_config.BiShengCompiler,
        download_config.GCCforOpenEuler,
        download_config.BiShengJDK8,
        download_config.BiShengJDK17,
    )
}

lkp_collection_map = {
    "LkpTests": {
        "download file": {
            URL: f"{download_config.LkpTests.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'lkp-tests.tar.gz')}",
        },
        "download gem dependency": {
            URL: f"{download_config.LkpTests.get('gem dependency')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'gem_dependencies.zip')}",
        },
        "download CompatibilityTesting": {
            URL: f"{download_config.CompatibilityTesting.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'compatibility_testing.tar.gz')}",
        }
    },
}
SCANNER = "scanner"
BUILDER = "builder"
EXECUTOR = "executor"

ROLE_COMPONENT = {
    SCANNER: ["BiShengJDK17"],
    BUILDER: ["GCCforOpenEuler", "BiShengCompiler", "BiShengJDK17", "BiShengJDK8"],
    EXECUTOR: ["BiShengJDK17", "LkpTests"]
}

ROLE_LIST = [SCANNER, BUILDER, EXECUTOR]


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


def download_dependence(component_list):
    if not os.path.exists(DEFAULT_PATH):
        os.mkdir(DEFAULT_PATH)
    elif os.path.isfile(DEFAULT_PATH):
        print(f"[ERROR] The file {DEFAULT_PATH} exists. Please rename or remove this file.")
        return False

    ret = True
    component_collection_map.update(lkp_collection_map)
    for component_name in component_collection_map:
        if component_name not in component_list:
            continue
        shell_dict = component_collection_map.get(component_name)
        ret = ret and download_dependence_handler(shell_dict)
    return ret


def download_a_fot():
    saved_path = os.path.join(DEFAULT_PATH, A_FOT)
    try:
        os.mkdir(saved_path)
    except FileExistsError as e:
        pass

    try:
        for f in FILE_LIST:
            wget.download(BASE_URL.format(f), os.path.join(saved_path, f))

        with tarfile.open(os.path.join(DEFAULT_PATH, "a-fot.tar.gz"), "w:gz") as t:
            t.add(saved_path, arcname="a-fot")
        return True
    except Exception as e:
        print(e)
        return False
    finally:
        shutil.rmtree(saved_path)


iso_collection_map = {
    component.get("component_name"): {
        "download file":
            {
                URL: f"{component.get(FILE)}",
                SAVE_PATH: f"{os.path.join('./', component.get(FILE).split('/')[-1])}"
            },
        "download sha256":
            {
                URL: f"{component.get(SHA256)}",
                SAVE_PATH: f"{os.path.join('./', component.get(SHA256).split('/')[-1])}"
            }
    } for component in (
        download_config.OpenEuler_2003_LTS,
        download_config.OpenEuler_2003_LTS_SP1,
        download_config.OpenEuler_2003_LTS_SP2,
        download_config.OpenEuler_2003_LTS_SP3,
        download_config.OpenEuler_2003_LTS_SP4,
        download_config.OpenEuler_2009,
        download_config.OpenEuler_2103,
        download_config.OpenEuler_2109,
        download_config.OpenEuler_2203_LTS,
        download_config.OpenEuler_2203_LTS_SP1,
        download_config.OpenEuler_2203_LTS_SP2,
        download_config.OpenEuler_2203_LTS_SP3,
        download_config.OpenEuler_2209,
        download_config.OpenEuler_2303,
        download_config.OpenEuler_2309,
    )
}


def download_iso():
    if platform.system() == "Windows" and CommandLine.download_iso == "auto":
        print("Please use '-iso' option in Linux machine if iso version is not specified. "
              "OpenEuler Operating System is recommended.")
        sys.exit(1)
    if CommandLine.download_iso == "auto":
        result = subprocess.run("grep PRETTY_NAME /etc/os-release".split(' '),
                                capture_output=True, shell=False)
        output = result.stdout.decode().strip()
        print(f"Get os-release output: {output}")

        CommandLine.download_iso = (output.split("=")[1]
                                    .replace("(", "")
                                    .replace(")", "")
                                    .replace(".", "")
                                    .replace("\"", "")
                                    .replace(" ", "_")
                                    .replace("-", "_"))
        print(f"Auto detect operating system version: {CommandLine.download_iso}")

    shell_dict = iso_collection_map.get(CommandLine.download_iso, "")
    if not shell_dict:
        print("Please check /etc/os-release is changed or not.")
        return False
    return download_dependence_file("download file", shell_dict)


def generate_component_list(yaml_dict):
    component_list = list()
    for role in ROLE_LIST:
        if role not in yaml_dict:
            continue
        component_list.extend(ROLE_COMPONENT[role])
    return list(set(component_list))


if __name__ == '__main__':
    try:
        process_command_line(program="download_dependency", description="devkit-pipeline download_dependency tool",
                             class_list=[CommandLine])
        if CommandLine.download_iso:
            if download_iso():
                print("-- Download iso success. --")
            else:
                print("Download iso failed.")
            sys.exit(0)
        config_dict = read_yaml_file(CommandLine.yaml_path)
        ret = download_dependence(generate_component_list(config_dict))
        if ret:
            print(f"Now compress dependencies to {DEPENDENCY_FILE}...")
            with tarfile.open(DEPENDENCY_FILE, "w:gz") as tar:
                tar.add(DEFAULT_PATH, arcname=os.path.basename(DEFAULT_PATH))

            print(f"-- Compress dependencies to {DEPENDENCY_FILE} success. --")
            shutil.rmtree(DEFAULT_PATH)
            print("-- Delete dependencies directory. --")
        else:
            print("-- Download dependencies failed. Please try execute download tool again. --")
    except (KeyboardInterrupt, Exception) as e:
        print(f"\nDownload dependencies failed. {str(e)} Please try execute download tool again.")
        sys.exit(1)
