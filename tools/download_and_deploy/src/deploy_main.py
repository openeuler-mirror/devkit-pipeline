import os
import subprocess
import sys
import logging

from log import config_logging
from deploy.deploy_command_line import process_command_line, CommandLine

from handler.pipeline import PipeLine
from handler.base_yaml_check import BaseCheck
from handler.connect_check import ConnectCheck
from handler.gather_package import GatherPackage
from handler.install_package import InstallPackage
from handler.result_report import ResultReport
from utils import read_yaml_file

LOGGER = logging.getLogger("deploy_tool")
PIPELINE = [BaseCheck(), ConnectCheck(), GatherPackage(), InstallPackage(), ResultReport()]
ISO_VERIFY_FLAG_STRING = "ISO 9660 CD-ROM filesystem data"


def check_iso_available(iso_path):
    if not os.path.isfile(iso_path):
        LOGGER.error(f"ISO file is not in specified path. Error: {iso_path} file not found.")
        sys.exit(1)
    try:
        result = subprocess.run(f"file -b {iso_path}".split(' '),
                                capture_output=True, shell=False)
        output = result.stdout.decode().strip()
        if output.find(ISO_VERIFY_FLAG_STRING) == -1:
            LOGGER.error(f"Verify iso result: Not available. Please re-download iso file.")
            sys.exit(1)
    except (FileNotFoundError, IsADirectoryError, PermissionError, Exception) as e:
        LOGGER.error(f"Verify iso file integrity occur error: {str(e)}")
        sys.exit(1)


if __name__ == '__main__':
    try:
        process_command_line(program="deploy_tool", description="devkit-pipeline deploy_tool",
                             class_list=[CommandLine])
        config_logging(CommandLine.silent)
        config_dict = read_yaml_file(CommandLine.yaml_path)
        LOGGER.debug(f"-- config_dict: {config_dict}")

        pipe = PipeLine(config_dict)
        pipe.add_tail(*PIPELINE)
        pipe.start()
    except (KeyboardInterrupt, Exception) as e:
        print(f"[warning] Program Exited. {str(e)}")
