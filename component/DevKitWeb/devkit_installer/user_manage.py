import logging
import requests
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


SERVER_PORT = "8086"
DEVKIT_VERSION = "v1.0"
FRAMEWORK_URL = "https://{}:{}/framework/api/{}"
LOGGER = logging.getLogger("devkit_installer")
NO_PROXY = {"http": None, "https": None}


class UserManage:
    def __init__(self, server_ip):
        self.server_ip = server_ip
        self.frame_url = FRAMEWORK_URL.format(self.server_ip, SERVER_PORT, DEVKIT_VERSION)

    def first_login(self, username, password):
        url = f"{self.frame_url}/users/admin-password/"
        response_body = {"username": username, "password": password, "confirm_password": password}
        try:
            result = requests.post(url=url, json=response_body, verify=False, timeout=10, proxies=NO_PROXY)
        except (requests.exceptions.ConnectionError,
                requests.exceptions.Timeout,
                requests.exceptions.HTTPError,
                requests.exceptions.ProxyError,
                requests.exceptions.TooManyRedirects,
                requests.exceptions.RequestException,
                urllib3.exceptions.MaxRetryError,
                OSError, Exception) as e:
            LOGGER.info("Admin first_login failed.")
            return {}
        LOGGER.info(f"POST Request result: {result.text}")
        result_dict = result.json()
        LOGGER.info("Admin first_login success.")
        return result_dict
