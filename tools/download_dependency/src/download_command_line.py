import argparse
import download_config


class CommandLine:
    download_iso = None

    @classmethod
    def add_options(cls, parser):
        parser.add_argument("-iso", action="store", dest="download_iso", default="",
                            choices=[
                                component.get("component_name") for component in (
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
                                    {"component_name": "auto"},
                                )
                            ],
                            metavar="SPECIFY_DOWNLOADING_ISO_VERSION",
                            help="Specify downloading iso version. "
                                 "Candidate iso versions: "
                                 "openEuler_2003_LTS, openEuler_2003_LTS_SP1, openEuler_2003_LTS_SP2, "
                                 "openEuler_2003_LTS_SP3, openEuler_2003_LTS_SP4, "
                                 "openEuler_2009, openEuler_2103, openEuler_2109, "
                                 "openEuler_2203_LTS, openEuler_2203_LTS_SP1, openEuler_2203_LTS_SP2, "
                                 "openEuler_2203_LTS_SP3, "
                                 "openEuler_2209, openEuler_2303, openEuler_2309. "
                                 "Input 'auto' will auto detect operating system version in Linux if iso version is not specified."
                            )

    @classmethod
    def process_args(cls, args):
        cls.download_iso = args.download_iso
        return cls.download_iso


def process_command_line(program, description, class_list):
    parser = argparse.ArgumentParser(prog=program, description=description, add_help=True)
    for klass in class_list:
        klass.add_options(parser)

    args = parser.parse_args()
    for klass in class_list:
        klass.process_args(args)
