import os.path
import sys


def check_is_running_in_pyinstaller_bundle():
    """
    判断是否在pyinstaller
    """
    if getattr(sys, "frozen", False) and hasattr(sys, "_MEIPASS"):
        return True
    else:
        return False


def obtain_root_path(root_path):
    """
    获取rootpath,当在pyinstaller中时，为父母录，否认为参入的参数
    """
    if check_is_running_in_pyinstaller_bundle():
        return os.path.dirname(os.path.dirname(sys.executable))
    else:
        return root_path
