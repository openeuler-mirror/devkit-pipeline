import os
import platform
import subprocess
import urllib.error
import warnings

import requests
import wget
from download import download_config
from constant import URL, FILE, SAVE_PATH, SHA256, DEFAULT_PATH

warnings.filterwarnings("ignore", message='Unverified HTTPS request')


component_collection_map = {
    component.get("component_name"): {
        "download file":
            {
                URL: f"{component.get(FILE)}",
                SAVE_PATH: f"{os.path.join(DEFAULT_PATH, component.get(FILE).split('/')[-1])}"
            },
        "download sha256":
            {
                URL: f"{component.get(SHA256)}",
                SAVE_PATH: f"{os.path.join(DEFAULT_PATH, component.get(SHA256).split('/')[-1])}"
            }
    } for component in (
        download_config.BiShengCompiler,
        download_config.GCCforOpenEuler,
        download_config.BiShengJDK8,
        download_config.BiShengJDK17,
    )
}

lkp_collection_map = {
    "LkpTests": {
        "download file": {
            URL: f"{download_config.LkpTests.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'lkp-tests.tar.gz')}",
        },
        "download gem dependency": {
            URL: f"{download_config.LkpTests.get('gem dependency')}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'gem_dependencies.zip')}",
        },
        "download CompatibilityTesting": {
            URL: f"{download_config.CompatibilityTesting.get(FILE)}",
            SAVE_PATH: f"{os.path.join(DEFAULT_PATH, 'compatibility_testing.tar.gz')}",
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
    component_collection_map.update(lkp_collection_map)
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
    if platform.system() == "Windows":
        if os.path.exists(save_path) and os.path.isfile(save_path):
            return
        wget.download(url, save_path)
        print()
    else:
        req_ = requests.get(url, stream=True, verify=False)
        total_size = int(req_.headers.get("Content-Length"))
        if os.path.exists(save_path) and os.path.getsize(save_path) == total_size:
            return
        subprocess.run(f"wget -c {url} -O {save_path} --no-check-certificate".split(' '),
                       capture_output=False, shell=False, stderr=subprocess.STDOUT)
        print()
