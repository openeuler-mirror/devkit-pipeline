import argparse

DEFAULT_YAML_PATH = "./machine.yaml"


class CommandLine:
    yaml_path = DEFAULT_YAML_PATH
    iso_path = None
    debug = False

    @classmethod
    def add_options(cls, parser):
        parser.add_argument("-f", "--config", action="store", dest="yaml_path", default=DEFAULT_YAML_PATH,
                            help="Assign yaml config file path. Default path is 'machine.yaml' in current directory.")
        parser.add_argument("-iso", action="store", dest="iso_path", default=None,
                            help="Assign prepared iso file path.")
        parser.add_argument("--debug", action="store_true", dest="debug", default=False, help="Open debug log.")

    @classmethod
    def process_args(cls, args):
        cls.yaml_path = args.yaml_path if args.yaml_path and args.yaml_path != "./" else DEFAULT_YAML_PATH
        cls.iso_path = args.iso_path
        cls.debug = args.debug
        return cls.yaml_path


def process_command_line(program, description, class_list):
    parser = argparse.ArgumentParser(prog=program, description=description, add_help=True)
    for klass in class_list:
        klass.add_options(parser)

    args = parser.parse_args()
    for klass in class_list:
        klass.process_args(args)
