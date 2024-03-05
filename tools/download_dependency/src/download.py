import os
import sys
import shutil
import tarfile
import urllib.error
import wget

from download_config import BiShengCompiler, GCCforOpenEuler, BiShengJDK8, BiShengJDK17

FILE = "file"
SHA256 = "sha256"
URL = "url"
SAVE_PATH = "save_path"
DEFAULT_PATH = "./devkitdependencies"
DEPENDENCY_FILE = "devkitdependencies.tar.gz"

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
        BiShengCompiler, GCCforOpenEuler, BiShengJDK8, BiShengJDK17
    )
}


def download_dependence():
    if not os.path.exists(DEFAULT_PATH):
        os.mkdir(DEFAULT_PATH)
    elif os.path.isfile(DEFAULT_PATH):
        print(f"[ERROR] The file {DEFAULT_PATH} exists. Please rename or remove this file.")
        return False
    else:
        pass

    ret = True
    for component_name in component_collection_map:
        shell_dict = component_collection_map.get(component_name)
        for shell_cmd in shell_dict:
            url_and_save_path = shell_dict.get(shell_cmd)
            try:
                print(f"Downloading from {url_and_save_path.get(URL)}")
                download_result = wget.download(
                    url_and_save_path.get(URL), url_and_save_path.get(SAVE_PATH)
                )
                print()
            except (TimeoutError, urllib.error.URLError, OSError) as e:
                print(f"[ERROR] download error occurs: {str(e)}")
                return False

            if not os.path.isfile(download_result):
                print(f"[ERROR] Download dependencies failed. "
                      f"Please visit following url and download dependencies to default directory."
                      f"\n\t{url_and_save_path.get(URL)}"
                      )
                ret = False
    return ret


if __name__ == '__main__':
    try:
        ret = download_dependence()
        if ret:
            print(f"Now compress dependencies to {DEPENDENCY_FILE}...")
            with tarfile.open(DEPENDENCY_FILE, "w:gz") as tar:
                tar.add(DEFAULT_PATH, arcname=os.path.basename(DEFAULT_PATH))

            print(f"-- Compress dependencies to {DEPENDENCY_FILE} success. --")
            shutil.rmtree(DEFAULT_PATH)
            print("-- Delete dependencies directory. --")
        else:
            print("-- Download dependencies failed. Please try execute download tool again. --")
    except (KeyboardInterrupt, Exception) as e:
        print(f"Download dependencies failed. {str(e)} Please try execute download tool again.")
        sys.exit(1)
