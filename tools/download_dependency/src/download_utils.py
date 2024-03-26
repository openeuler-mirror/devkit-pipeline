import os
import sys
import urllib.error
import wget


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

