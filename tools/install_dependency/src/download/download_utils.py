import os
import platform
import subprocess
import urllib.error
import warnings

import requests
import wget

from constant import URL, SAVE_PATH, FILE, SHA256, FILE_SIZE, DEFAULT_PATH
from download import download_config

warnings.filterwarnings("ignore", message='Unverified HTTPS request')


component_collection_map = {
    "BiShengCompiler": {
        "download file": {
            URL: f"{download_config.BiShengCompiler.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengCompiler.get(FILE).split('/')[-1])}",
            FILE_SIZE: "1051195289",
        },
        "download sha256": {
            URL: f"{download_config.BiShengCompiler.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengCompiler.get(SHA256).split('/')[-1])}",
            FILE_SIZE: "107",
        },
    },
    "GCCforOpenEuler": {
        "download file": {
            URL: f"{download_config.GCCforOpenEuler.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.GCCforOpenEuler.get(FILE).split('/')[-1])}",
            FILE_SIZE: "274901693",
        },
        "download sha256": {
            URL: f"{download_config.GCCforOpenEuler.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.GCCforOpenEuler.get(SHA256).split('/')[-1])}",
            FILE_SIZE: "106",
        },
    },
    "BiShengJDK8": {
        "download file": {
            URL: f"{download_config.BiShengJDK8.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK8.get(FILE).split('/')[-1])}",
            FILE_SIZE: "117055434",
        },
        "download sha256": {
            URL: f"{download_config.BiShengJDK8.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK8.get(SHA256).split('/')[-1])}",
            FILE_SIZE: "105",
        },
    },
    "BiShengJDK17": {
        "download file": {
            URL: f"{download_config.BiShengJDK17.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK17.get(FILE).split('/')[-1])}",
            FILE_SIZE: "196772672",
        },
        "download sha256": {
            URL: f"{download_config.BiShengJDK17.get(SHA256)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.BiShengJDK17.get(SHA256).split('/')[-1])}",
            FILE_SIZE: "107",
        },
    },

    "LkpTests": {
        "download file": {
            URL: f"{download_config.LkpTests.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get(FILE).split('/')[-1])}",
            FILE_SIZE: "29333270",
        },
        "GemDependency": {
            URL: f"{download_config.LkpTests.get('GemDependency')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get('GemDependency').split('/')[-1])}",
            FILE_SIZE: "4206309",
        },
        "CompatibilityTesting": {
            URL: f"{download_config.LkpTests.get('CompatibilityTesting')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.LkpTests.get('CompatibilityTesting').split('/')[-1])}",
            FILE_SIZE: "76645477",
        },
        
    },

    "DevkitDistribute": {
        "download file": {
            URL: f"{download_config.DevkitDistribute.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.DevkitDistribute.get(FILE).split('/')[-1])}",
            FILE_SIZE: "13349694",
        }
    },

    "A-FOT": {
        "download file": {
            URL: f"{download_config.A_FOT.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, download_config.A_FOT.get(FILE).split('/')[-1])}",
            FILE_SIZE: "15740",
        }
    },

    "DevKitWeb": {
        "download file": {
            URL: f"{download_config.DevKitWeb.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'DevKit-All-23.0.T30-Linux-Kunpeng.tar.gz')}",
            FILE_SIZE: "1143374523",
        },
    },

    "DevKitCLI": {
        "download file": {
            URL: f"{download_config.DevKitCLI.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'DevKit-CLI-24.0.RC1-Linux-Kunpeng.tar.gz')}",
            FILE_SIZE: "300000000",
        }
    },
}


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
    file_size = url_and_save_path.get("file_size")
    try:
        print(f"Downloading from {url_}")
        download(url_, save_path, file_size)
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


def download(url, save_path, file_size):
    if os.path.exists(save_path) and os.path.isfile(save_path) and str(os.path.getsize(save_path)) == file_size:
        return
    if platform.system() == "Windows":
        wget.download(url, save_path)
        print()
    else:
        subprocess.run(f"wget -c {url} -O {save_path} --no-check-certificate".split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)
        print()
