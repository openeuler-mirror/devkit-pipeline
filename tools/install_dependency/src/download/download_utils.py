import os
import urllib.error
import wget
from download import download_config
from constant import URL, FILE, SAVE_PATH, SHA256, DEFAULT_PATH


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
        ret = download_dependence_file(shell_cmd, shell_dict)
    return ret


def download_dependence_file(shell_cmd, shell_dict):
    ret = True
    url_and_save_path = shell_dict.get(shell_cmd)
    if os.path.exists(url_and_save_path.get('save_path')) and os.path.isfile(url_and_save_path.get('save_path')):
        return ret
    try:
        print(f"Downloading from {url_and_save_path.get('url')}")
        download_result = wget.download(
            url_and_save_path.get('url'), url_and_save_path.get('save_path')
        )
        print()
    except (TimeoutError, urllib.error.URLError, OSError) as e:
        print(f"[ERROR] download error occurs: {str(e)} "
              f"\nPlease visit following url and download dependencies to default directory."
              f"\n\t{url_and_save_path.get('url')}"
              )
        raise OSError(f"download error occurs: {str(e)}")

    if not os.path.isfile(download_result):
        print(f"[ERROR] Download dependencies failed. "
              f"Please visit following url and download dependencies to default directory."
              f"\n\t{url_and_save_path.get('url')}"
              )
        ret = False
    return ret

