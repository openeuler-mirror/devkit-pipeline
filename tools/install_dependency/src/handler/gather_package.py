import logging
import os
import subprocess
import constant
from download.download_utils import download_dependence
from handler.handler_and_node import Handler
from utils import generate_component_list

LOGGER = logging.getLogger("install_dependency")


class GatherPackage(Handler):
    def __init__(self):
        super(GatherPackage, self).__init__()
        self.component_list = list()

    def handle(self, data) -> bool:
        LOGGER.debug("GatherPackage start!")
        self.component_list = generate_component_list(data)

        try:
            self.check_dependency()
        except Exception as e:
            return False

        try:
            ret = download_dependence(self.component_list)
        except Exception as e:
            LOGGER.error(f"Download dependencies failed. {str(e)}. Please execute download tool.")
            return False
        if not ret:
            LOGGER.error("Download dependencies failed. Please execute download tool.")
            return False
        LOGGER.info("Download dependencies success.")
        return True

    @staticmethod
    def check_dependency():
        if os.path.exists(constant.DEPENDENCY_FILE):
            try:
                print(f"Now extract files from {constant.DEPENDENCY_FILE}:")
                subprocess.run(f"tar -zxvf {constant.DEPENDENCY_FILE}".split(' '),
                               capture_output=False, shell=False, stderr=subprocess.STDOUT)
            except (FileExistsError,) as e:
                LOGGER.warning(f"{constant.DEPENDENCY_FILE} may already extracted.")
            except Exception as e:
                LOGGER.error(f"Extract {constant.DEPENDENCY_FILE} failed. {str(e)}")
                raise
        if os.path.isfile(constant.DEPENDENCY_DIR):
            LOGGER.error(f"The file {constant.DEPENDENCY_DIR} exists. Please rename or remove this file.")
            raise Exception
