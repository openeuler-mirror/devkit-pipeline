import argparse

PACKAGE_NAME = "DevKit-All-24.0.RC1-Linux-Kunpeng.tar.gz"


class CommandLine:
    ip = ""
    user = ""
    pkey = ""
    package_path = "/opt"
    package_name = PACKAGE_NAME
    admin_username = "devadmin"
    admin_password = "devkit123"
    install_path = "/opt"
    debug = False

    @classmethod
    def add_options(cls, parser):
        parser.add_argument("-i", "--ip", action="store", dest="ip",
                            help="Assign IP to install DevKit.")
        parser.add_argument("-u", "--user", action="store", dest="user",
                            help="Assign USER configured password-free.")
        parser.add_argument("-p", "--pkey", action="store", dest="pkey",
                            help="Assign PKEY used for password-free.")
        parser.add_argument("-papath", "--package-path", action="store", dest="package_path", default="/opt",
                            help="Assign DevKit package store path. Default DevKit package store path is '/opt'.")
        parser.add_argument("-paname", "--package-name", action="store", dest="package_name", default=PACKAGE_NAME,
                            help=f"Assign DevKit package name. Default DevKit package name is '{PACKAGE_NAME}'.")
        parser.add_argument("-aduser", "--admin-username", action="store", dest="admin_username", default="devadmin",
                            help="Assign DevKit admin username. Default admin username is 'devadmin'.")
        parser.add_argument("-adpass", "--admin-password", action="store", dest="admin_password", default="devkit123",
                            help="Assign DevKit admin password. Default admin password is 'devkit123'.")
        parser.add_argument("-inpath", "--install-path", action="store", dest="install_path", default="/opt",
                            help="Assign DevKit install path. Default DevKit install path is '/opt'.")
        parser.add_argument("--debug", action="store_true", dest="debug", default=False, help=argparse.SUPPRESS)

    @classmethod
    def process_args(cls, args):
        cls.ip = args.ip
        cls.user = args.user
        cls.pkey = args.pkey
        cls.package_path = args.package_path
        cls.package_name = args.package_name
        cls.admin_username = args.admin_username
        cls.admin_password = args.admin_password
        cls.install_path = args.install_path
        cls.debug = args.debug
        return (cls.ip and cls.user and cls.pkey
                and cls.package_path and cls.package_name
                and cls.admin_username and cls.admin_password and cls.install_path)


def process_command_line(program, description, class_list):
    parser = argparse.ArgumentParser(prog=program, description=description, add_help=True)
    for klass in class_list:
        klass.add_options(parser)

    args = parser.parse_args()
    for klass in class_list:
        if not klass.process_args(args):
            return False
    return True
