import sys
import logging
import yaml

from log import config_logging
from command_line import process_command_line, CommandLine

LOGGER = logging.getLogger("install_dependency")


def read_yaml_file(yaml_path):
    try:
        with open(yaml_path, "r") as file:
            config_dict = yaml.safe_load(file)
    except (FileNotFoundError, IsADirectoryError) as e:
        LOGGER.error(f"Yaml file is not in specified path. Error: {str(e)}")
        sys.exit(1)
    except (yaml.parser.ParserError,
            yaml.scanner.ScannerError,
            yaml.composer.ComposerError,
            yaml.constructor.ConstructorError) as e:
        LOGGER.error(f"Incorrect yaml file. Error: {str(e)}")
        sys.exit(1)
    return config_dict


if __name__ == '__main__':
    try:
        process_command_line(program="install_dependency", description="devkit-pipeline install_dependency tool",
                             class_list=[CommandLine])
        config_logging(CommandLine.debug)
        config_dict = read_yaml_file(CommandLine.yaml_path)
        LOGGER.debug(f"-- config_dict: {config_dict}")

    except (KeyboardInterrupt, Exception) as e:
        print(f"[warning] Program Exited. {str(e)}")
