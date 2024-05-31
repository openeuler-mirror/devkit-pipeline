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
    component.get("component_name"): {
        URL: f"{component.get(FILE)}",
        SAVE_PATH: f"{os.path.join(DEFAULT_PATH, component.get(FILE).split('/')[-1])}",
        FILE_SIZE: f"{component.get(FILE_SIZE)}",
    } for component in (
        download_config.BiShengCompiler,
        download_config.GCCforOpenEuler,
        download_config.BiShengJDK8,
        download_config.BiShengJDK17,
        download_config.CompatibilityTesting,
        download_config.DevKitTester,
        download_config.DevKitWeb,
        download_config.DevKitCLI,
        download_config.A_FOT
    )
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
        url_and_save_path = component_collection_map.get(component_name)
        ret = ret and download_dependence_file(url_and_save_path)
    return ret


def download_dependence_file(url_and_save_path):
    ret = True
    url_ = url_and_save_path.get("url")
    save_path = url_and_save_path.get("save_path")
    file_size = url_and_save_path.get("file_size")
    try:
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

    if not os.path.isfile(save_path) or not str(os.path.getsize(save_path)) == file_size:
        print(f"[ERROR] Download dependencies failed. "
              f"Please visit following url and download dependencies to default directory."
              f"\n\t{url_}"
              )
        ret = False
    return ret


def download(url, save_path, file_size):
    if os.path.exists(save_path) and os.path.isfile(save_path) and str(os.path.getsize(save_path)) == file_size:
        return
    print(f"Downloading from {url}")
    if platform.system() == "Windows":
        wget.download(url, save_path)
        print()
    else:
        subprocess.run(f"wget -c {url} -O {save_path} --no-check-certificate".split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)
        print()
