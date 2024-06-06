import copy
import os.path
import sys


class PyInstallerUtils:
    ORI_ENV = None
    LD_LIBRARY_PATH_ORIG = "LD_LIBRARY_PATH_ORIG"
    LD_LIBRARY_PATH = "LD_LIBRARY_PATH"

    @classmethod
    def check_is_running_in_pyinstaller_bundle(cls):
        """
        判断是否在pyinstaller
        """
        if getattr(sys, "frozen", False) and hasattr(sys, "_MEIPASS"):
            return True
        else:
            return False

    @classmethod
    def obtain_root_path(cls, root_path):
        """
        获取rootpath,当在pyinstaller中时，为父目录，否则为传入的参数
        """
        if cls.check_is_running_in_pyinstaller_bundle():
            return os.path.dirname(os.path.dirname(sys.executable))
        else:
            return root_path

    @classmethod
    def get_env(cls):
        """
        返回的env,不允许修改
        """
        if cls.ORI_ENV:
            return cls.ORI_ENV
        env_copy = copy.deepcopy(os.environ)
        if cls.check_is_running_in_pyinstaller_bundle():
            if cls.LD_LIBRARY_PATH_ORIG in env_copy:
                env_copy[cls.LD_LIBRARY_PATH] = env_copy.get(cls.LD_LIBRARY_PATH_ORIG)
            else:
                env_copy.pop(cls.LD_LIBRARY_PATH)
        cls.ORI_ENV = env_copy
        return cls.ORI_ENV
