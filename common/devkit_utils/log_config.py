import logging.config
import os

from devkit_utils import file_utils
from devkit_utils import shell_tools


def config_log_ini(root_path, log_name):
    log_template = os.path.join(root_path, "config/log.ini.template")
    log_config = os.path.join(root_path, "config/log.ini")
    log_path = os.path.join(root_path, "log")
    log_replace_path = log_path.replace("/", "\\/")
    shell_tools.exec_shell("cp {} {}".format(log_template, log_config), is_shell=True)
    shell_tools.exec_shell("sed -i \"s/LOG_PATH/{}/g\" {} ".format(log_replace_path, log_config), is_shell=True)
    shell_tools.exec_shell("sed -i \'s/LOG_NAME/{}/g\' {} ".format(log_name, log_config), is_shell=True)
    file_utils.create_dir(log_path)
    logging.config.fileConfig(os.path.join(root_path, "config/log.ini"))
