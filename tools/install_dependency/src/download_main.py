import os
import platform
import subprocess
import sys

from download import download_config
from download.download_utils import download_dependence_file
from download.download_command_line import process_command_line, CommandLine
from handler.pipeline import PipeLine
from handler.base_yaml_check import BaseCheck
from handler.gather_package import GatherPackage
from handler.compress_dep import CompressDep

from utils import read_yaml_file
from constant import URL, SAVE_PATH, FILE, SHA256, FILE_SIZE, INSTRUCTION


PIPELINE = [BaseCheck(), GatherPackage(), CompressDep()]


iso_collection_map = {
    component.get("component_name"): {
        "download file": {
            URL: f"{component.get(FILE)}",
            SAVE_PATH: f"{os.path.join('./', component.get(FILE).split('/')[-1])}",
            FILE_SIZE: f"{component.get(FILE_SIZE)}",
        },
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
        config_dict[INSTRUCTION] = "default"
        pipe = PipeLine(config_dict)
        pipe.add_tail(*PIPELINE)
        pipe.start()

    except (KeyboardInterrupt, Exception) as e:
        print(f"\nDownload dependencies failed. {str(e)} Please try execute download tool again.")
        sys.exit(1)
