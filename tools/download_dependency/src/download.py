import os
import platform
import subprocess
import sys
import shutil
import tarfile
import time

import download_config
from download_utils import download_dependence_handler, download_dependence_file
from download_command_line import process_command_line, CommandLine

FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
DEFAULT_PATH = "./devkitdependencies"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"

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
        download_config.BiShengJDK17
    )
}


def download_dependence():
    if not os.path.exists(DEFAULT_PATH):
        os.mkdir(DEFAULT_PATH)
    elif os.path.isfile(DEFAULT_PATH):
        print(f"[ERROR] The file {DEFAULT_PATH} exists. Please rename or remove this file.")
        return False
    else:
        pass

    ret = True
    for component_name in component_collection_map:
        shell_dict = component_collection_map.get(component_name)
        ret = ret and download_dependence_handler(shell_dict)
    return ret


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
        time.sleep(10)
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

        ret = download_dependence()
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
