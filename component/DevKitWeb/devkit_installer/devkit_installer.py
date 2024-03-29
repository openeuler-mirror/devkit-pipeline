import logging
import sys

from user_manage import UserManage
from devkit_machine import DevKitMachine
from installer_log import config_logging
from installer_command_line import process_command_line, CommandLine

LOGGER = logging.getLogger("devkit_installer")


class DevKitInstaller:
    def __init__(self):
        self.server_ip = CommandLine.ip
        self.user = CommandLine.user
        self.pkey = CommandLine.pkey
        self.package_path = CommandLine.package_path
        self.package_name = CommandLine.package_name
        self.admin_username = CommandLine.admin_username
        self.admin_password = CommandLine.admin_password
        self.install_path = CommandLine.install_path
        self.machine = DevKitMachine(ip=self.server_ip, user=self.user, pkey=self.pkey)
        self.user_manage = UserManage(server_ip=self.server_ip)

    def pre_install(self):
        LOGGER.info("Install DevKit start!")
        decompress_result = self.machine.decompress_package(
            package_path=self.package_path,
            package_name=self.package_name,
        )
        if not decompress_result:
            LOGGER.error("Decompress package failed.")
            exit(1)
        LOGGER.info("Decompress package success.")

    def install_java_perf(self):
        install_result = self.machine.devkit_install_by_cmd(server_ip=self.server_ip,
                                                            server_port="8086",
                                                            http_port="8002",
                                                            install_path=self.install_path,
                                                            plugin="java_perf",
                                                            rpc_port=50051)
        if not install_result:
            LOGGER.error(f"Install DevKit failed.")
            exit(1)
        LOGGER.info(f"Install DevKit success.")

    def set_up_password(self):
        # 初始化管理员密码
        result_dict = self.user_manage.first_login(username=self.admin_username, password=self.admin_password)
        if not result_dict:
            exit(1)
        LOGGER.info("DevKitInstaller executive success.")


if __name__ == '__main__':
    try:
        if not process_command_line(program="devkit_installer", description="devkit-pipeline devkit_installer",
                                    class_list=[CommandLine]):
            print("[ERROR] Command line params incomplete!")
            sys.exit(1)
        config_logging(CommandLine.debug)

        installer = DevKitInstaller()
        installer.pre_install()
        installer.install_java_perf()
        installer.set_up_password()

    except (KeyboardInterrupt, Exception) as e:
        print(f"[warning] Program Exited. {str(e)}")
        sys.exit(1)
