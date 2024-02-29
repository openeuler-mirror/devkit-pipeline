import logging
import os
import subprocess
import constant
from download import download_dependence, component_collection_map
from handler.handler_and_node import Handler

LOGGER = logging.getLogger("install_dependency")


class GatherPackage(Handler):
    def handle(self, data) -> bool:
        LOGGER.debug("GatherPackage start!")
        if GatherPackage.check_default_path_available():
            LOGGER.info("Dependencies ready.")
            return True
        
        if os.path.isfile(constant.DEPENDENCY_DIR):
            LOGGER.error(f"The file {constant.DEPENDENCY_DIR} exists. Please rename or remove this file.")
            return False
        
        try:
            ret = download_dependence()
        except Exception as e:
            LOGGER.error(f"Download dependencies failed. {str(e)}. Please execute download tool.")
            return False
        if not ret:
            LOGGER.error("Download dependencies failed. Please execute download tool.")
            return False
        LOGGER.info("Download dependencies success.")
        return True
    
    @staticmethod
    def check_default_path_available():
        if os.path.exists(constant.DEPENDENCY_FILE):
            try:
                print(f"Now extract files from {constant.DEPENDENCY_FILE}:")
                result = subprocess.run(f"tar -zxvf {constant.DEPENDENCY_FILE}".split(' '),
                                        capture_output=False, shell=False, stderr=subprocess.STDOUT)
                print(f"{result.stdout}" if result.stdout else "")
            except (FileExistsError, ) as e:
                LOGGER.warning(f"{constant.DEPENDENCY_FILE} may already extracted.")
            except Exception as e:
                LOGGER.error(f"Extract {constant.DEPENDENCY_FILE} failed. {str(e)}")
                return False

        if not os.path.isdir(constant.DEPENDENCY_DIR):
            LOGGER.warning(f"The directory {constant.DEPENDENCY_DIR} not exists.")
            return False
        for component_name in component_collection_map:
            shell_dict = component_collection_map.get(component_name)
            for shell_cmd in shell_dict:
                url_and_save_path = shell_dict.get(shell_cmd)
                component = url_and_save_path.get("save_path")
                if not os.path.isfile(component):
                    LOGGER.warning(f"The file {component} not exists.")
                    return False
        return True
