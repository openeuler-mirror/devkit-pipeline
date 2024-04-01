import os
import platform
import subprocess
import shutil
import tarfile
import urllib.error
import warnings

import requests
import wget
from download import download_config
from constant import URL, FILE, SAVE_PATH, SHA256, DEFAULT_PATH


warnings.filterwarnings("ignore", message='Unverified HTTPS request')

# A-FOT files
BASE_URL = "https://gitee.com/openeuler/A-FOT/raw/master/{}"
A_FOT = "a-fot"
A_FOT_INI = "a-fot.ini"
AUTO_FDO_SH = "auto_fdo.sh"
AUTO_BOLT_SH = "auto_bolt.sh"
AUTO_PREFETCH = "auto_prefetch.sh"
SPLIT_JSON_PY = "split_json.py"
FILE_LIST = (A_FOT, A_FOT_INI, AUTO_FDO_SH, AUTO_BOLT_SH, AUTO_PREFETCH, SPLIT_JSON_PY)


component_collection_map = {
    "BiShengCompiler": {
        "download file": {
            URL: f"{download_config.BiShengCompiler.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengCompiler.get(FILE).split('/')[-1])}",
        },
        "download sha256": {
            URL: f"{download_config.BiShengCompiler.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengCompiler.get(SHA256).split('/')[-1])}",
        },
    },
    "GCCforOpenEuler": {
        "download file": {
            URL: f"{download_config.GCCforOpenEuler.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.GCCforOpenEuler.get(FILE).split('/')[-1])}",
        },
        "download sha256": {
            URL: f"{download_config.GCCforOpenEuler.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.GCCforOpenEuler.get(SHA256).split('/')[-1])}",
        },
    },
    "BiShengJDK8": {
        "download file": {
            URL: f"{download_config.BiShengJDK8.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK8.get(FILE).split('/')[-1])}",
        },
        "download sha256": {
            URL: f"{download_config.BiShengJDK8.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK8.get(SHA256).split('/')[-1])}",
        },
    },
    "BiShengJDK17": {
        "download file": {
            URL: f"{download_config.BiShengJDK17.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK17.get(FILE).split('/')[-1])}",
        },
        "download sha256": {
            URL: f"{download_config.BiShengJDK17.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK17.get(SHA256).split('/')[-1])}",
        },
    },

    "LkpTests": {
        "download file": {
            URL: f"{download_config.LkpTests.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get(FILE).split('/')[-1])}",
        },
        "GemDependency": {
            URL: f"{download_config.LkpTests.get('GemDependency')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get('GemDependency').split('/')[-1])}",
        },
        "CompatibilityTesting": {
            URL: f"{download_config.LkpTests.get('CompatibilityTesting')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get('CompatibilityTesting').split('/')[-1])}",
        },
        "DevkitDistribute": {
            URL: f"{download_config.LkpTests.get('DevkitDistribute')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get('DevkitDistribute').split('/')[-1])}",
        }
    },

    "DevKitWeb": {
        "download file": {
            URL: f"{download_config.DevKitWeb.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.DevKitWeb.get(FILE).split('/')[-1])}",
        },
    }
}


def download_a_fot():
    tar_file = os.path.join(DEFAULT_PATH, "a-fot.tar.gz")
    if os.path.exists(tar_file) and os.path.isfile(tar_file):
        return True

    saved_path = os.path.join(DEFAULT_PATH, A_FOT)
    try:
        os.mkdir(saved_path)
    except FileExistsError as e:
        pass

    try:
        for f in FILE_LIST:
            print(f"Downloading from {BASE_URL.format(f)}")
            wget.download(BASE_URL.format(f), os.path.join(saved_path, f))
            print()
        with tarfile.open(tar_file, "w:gz") as t:
            t.add(saved_path, arcname="a-fot")
        return True
    except Exception as e:
        print(e)
        return False
    finally:
        shutil.rmtree(saved_path)


def download_dependence(component_list):
    if not os.path.exists(DEFAULT_PATH):
        os.mkdir(DEFAULT_PATH)
    elif os.path.isfile(DEFAULT_PATH):
        print(f"[ERROR] The file {DEFAULT_PATH} exists. Please rename or remove this file.")
        return False

    ret = True
    for component_name in component_collection_map:
        if component_name not in component_list:
            continue
        shell_dict = component_collection_map.get(component_name)
        ret = ret and download_dependence_handler(shell_dict)
    if "A-FOT" in component_list:
        ret &= download_a_fot()
    return ret


def download_dependence_handler(shell_dict):
    ret = True
    for shell_cmd in shell_dict:
        try:
            ret = ret and download_dependence_file(shell_cmd, shell_dict)
        except Exception as e:
            ret = False
    return ret


def download_dependence_file(shell_cmd, shell_dict):
    ret = True
    url_and_save_path = shell_dict.get(shell_cmd)
    url_ = url_and_save_path.get("url")
    save_path = url_and_save_path.get("save_path")
    try:
        print(f"Downloading from {url_}")
        download(url_, save_path)
    except (requests.exceptions.Timeout,
            requests.exceptions.ConnectionError,
            requests.exceptions.HTTPError,
            requests.exceptions.TooManyRedirects,
            requests.exceptions.RequestException,
            subprocess.CalledProcessError,
            ValueError,
            TimeoutError,
            urllib.error.URLError,
            OSError, IOError) as e:
        print(f"[ERROR] download error occurs: {str(e)} "
              f"\nPlease visit following url and download dependencies to default directory."
              f"\n\t{url_}"
              )
        raise OSError(f"download error occurs: {str(e)}")

    if not os.path.isfile(save_path):
        print(f"[ERROR] Download dependencies failed. "
              f"Please visit following url and download dependencies to default directory."
              f"\n\t{url_}"
              )
        ret = False
    return ret


def download(url, save_path):
    if os.path.exists(save_path) and os.path.isfile(save_path):
        return
    if platform.system() == "Windows":
        wget.download(url, save_path)
        print()
    else:
        subprocess.run(f"wget -c {url} -O {save_path} --no-check-certificate".split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)
        print()
