import platform
import tarfile
import os
import shutil
from handler.handler_and_node import Handler
from constant import DEPENDENCY_FILE, DEFAULT_PATH


class CompressDep(Handler):
    def handle(self, _):
        if platform.system() == "Linux":
            return True

        try:
            print(f"Now compress dependencies to {DEPENDENCY_FILE}...")
            with tarfile.open(DEPENDENCY_FILE, "w:gz") as tar:
                tar.add(DEFAULT_PATH, arcname=os.path.basename(DEFAULT_PATH))
            print(f"-- Compress dependencies to {DEPENDENCY_FILE} success. --")
            shutil.rmtree(DEFAULT_PATH)
            print("-- Delete dependencies directory. --")
            return True
        except Exception as e:
            print(str(e))
            return False
