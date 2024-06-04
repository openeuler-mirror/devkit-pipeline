from log import config_logging
from pipeline_script_generator.script_generator_command_line import process_command_line, CommandLine

from handler.pipeline import PipeLine
from handler.base_yaml_check import BaseCheck
from handler.generate_pipeline_script import GeneratePipelineScript
from constant import INSTRUCTION

from utils import read_yaml_file

PIPELINE = [BaseCheck(), GeneratePipelineScript()]


if __name__ == '__main__':
    try:
        process_command_line(program="script_generator", description="devkit-pipeline script generator",
                             class_list=[CommandLine])
        config_logging(CommandLine.silent)
        config_dict = read_yaml_file(CommandLine.yaml_path)
        config_dict[INSTRUCTION] = "default"
        pipe = PipeLine(config_dict)
        pipe.add_tail(*PIPELINE)
        pipe.start()
    except (KeyboardInterrupt, Exception) as e:
        print(f"[warning] Program Exited. {str(e)}")
