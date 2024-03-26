import logging
import os
import subprocess
import constant
from download import download_dependence, component_collection_map, lkp_collection_map
from handler.handler_and_node import Handler

LOGGER = logging.getLogger("install_dependency")


class GatherPackage(Handler):
    def __init__(self):
        super(GatherPackage, self).__init__()
        self.component_list = list()

    def generate_component_list(self, data):
        component_list = []
        for _, machine in data[constant.MACHINE].items():
            component_list.extend(machine.get_components())
        self.component_list = list(set(component_list))

    def handle(self, data) -> bool:
        LOGGER.debug("GatherPackage start!")
        component_collection_map.update(lkp_collection_map)
        self.generate_component_list(data)

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
