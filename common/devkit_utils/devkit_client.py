import logging
import os.path

import requests
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)


class DevKitClient:
    NO_PROXY = {"http": None, "https": None}

    def __init__(self, ip, port, username, password):
        self.ip = ip
        self.port = port
        self.username = username
        self.password = password
        self.user_id = ""
        self.token = ""
        self.use_proxy = True
        self.header = dict()
        self.login()

    def __del__(self):
        self.logout()

    def __str__(self):
        return f"{self.ip}, {self.port}, {self.username}, {self.password}, {self.user_id}, {self.token}, f{self.header}"

    def login(self):
        url = f"https://{self.ip}:{self.port}/framework/api/v1.0/users/session/"
        body = dict({"username": self.username, "password": self.password})
        try:
            self.try_to_login(url, body)
        except Exception as err:
            self.use_proxy = False
            self.try_to_login(url, body)

    def try_to_login(self, url, body):
        try:
            if self.use_proxy:
                ret = requests.post(url=url, json=body, verify=False, timeout=10)
            else:
                ret = requests.post(url=url, json=body, verify=False, timeout=10, proxies=self.NO_PROXY)
            user_dict = ret.json()
            self.token = ret.headers["token"]
            self.user_id = user_dict["data"]["id"]
            self.header = dict({
                "Authorization": self.token,
                "Content-Type": "application/json",
                "Accept-Language": "zh-cn"
            })
        except requests.exceptions.ReadTimeout or requests.exceptions.ConnectionError as ex:
            logging.exception(ex)
            raise ex

    def logout(self):
        url = f"https://{self.ip}:{self.port}/framework/api/v1.0/users/session/{self.user_id}/"
        try:
            if self.use_proxy:
                requests.delete(url, headers=self.header, verify=False)
            else:
                requests.delete(url, headers=self.header, verify=False, proxies=self.NO_PROXY)
        except Exception as ex:
            logging.exception(ex)
            pass

    def upload_report(self, file_path):
        try:
            data = dict({"file": (os.path.basename(file_path), open(file_path, "rb").read())})
        except OSError as e:
            logging.exception(e)
            raise
        encoded_data = urllib3.encode_multipart_formdata(data)
        _header = self.header.copy()
        _header.update({"Content-Type": encoded_data[1]})
        url = f"https://{self.ip}:{self.port}/plugin/api/v1.0/java_perf/api/records/actions/upload/"
        if self.use_proxy:
            return requests.post(url=url, headers=_header, data=encoded_data[0], verify=False)
        else:
            return requests.post(url=url, headers=_header, data=encoded_data[0], verify=False, proxies=self.NO_PROXY)

    def get_record_list(self):
        url = f"https://{self.ip}:{self.port}/plugin/api/v1.0/java_perf/api/records/user/"
        data = {"userId": self.user_id}
        if self.use_proxy:
            return requests.post(url=url, json=data, headers=self.header, verify=False)
        else:
            return requests.post(url=url, json=data, headers=self.header, verify=False, proxies=self.NO_PROXY)

    def delete_report(self, task_id):
        url = f"https://{self.ip}:{self.port}/plugin/api/v1.0/java_perf/api/records/{task_id}/"
        if self.use_proxy:
            requests.delete(url=url, headers=self.header, verify=False)
        else:
            requests.delete(url=url, headers=self.header, verify=False, proxies=self.NO_PROXY)

    def upload_report_by_force(self, file_path):
        ret = self.upload_report(file_path)
        if ret.status_code == requests.codes.ok:
            return
        if ret.json().get("code", "") == "JavaPerf.Upload.Recording.RecordingReachLimit":
            records = self.get_record_list()
            task_id, create_time = "", "999999999999999999999999999999"
            for o in records.json().get("members", []):
                if float(o["createTime"]) < float(create_time):
                    task_id, create_time = o["id"], o["createTime"]
            self.delete_report(task_id)
            self.upload_report(file_path)


if __name__ == "__main__":
    try:
        d = DevKitClient("172.39.173.2", "8086", "devadmin", "Huawei12#$")
        d.upload_report_by_force("/home/panlonglong/Downloads/Main(136462)")
        d.logout()
    except Exception as e:
        print(str(e))
