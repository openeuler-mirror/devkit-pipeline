import os.path
import requests
from urllib3 import encode_multipart_formdata


class DevKit:
    def __init__(self, ip, port, username, password):
        self.ip = ip
        self.port = port
        self.username = username
        self.password = password
        self.user_id = ""
        self.token = ""
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
            ret = requests.post(url=url, json=body, verify=False, timeout=10)
            user_dict = ret.json()
            self.token = ret.headers["token"]
            self.user_id = user_dict["data"]["id"]
            self.header = dict({
                "Authorization": self.token,
                "Content-Type": "application/json",
                "Accept-Language": "zh-cn"
            })
        except requests.exceptions.ReadTimeout or requests.exceptions.ConnectionError as e:
            print(str(e))
            raise

    def logout(self):
        url = f"https://{self.ip}:{self.port}/framework/api/v1.0/users/session/{self.user_id}/"
        try:
            requests.delete(url, headers=self.header, verify=False)
        except Exception as e:
            pass

    def upload_report(self, file_path):
        try:
            data = dict({"file": (os.path.basename(file_path), open(file_path, "rb").read())})
        except OSError as e:
            print(str(e))
            raise
        encoded_data = encode_multipart_formdata(data)
        _header = self.header.copy()
        _header.update({"Content-Type": encoded_data[1]})
        url = f"https://{self.ip}:{self.port}/plugin/api/v1.0/java_perf/api/records/actions/upload/"
        return requests.post(url=url, headers=_header, data=encoded_data[0], verify=False)

    def get_record_list(self):
        url = f"https://{self.ip}:{self.port}/plugin/api/v1.0/java_perf/api/records/user/"
        data = {"userId": self.user_id}
        return requests.post(url=url, json=data, headers=self.header, verify=False)

    def delete_report(self, task_id):
        url = f"https://{self.ip}:{self.port}/plugin/api/v1.0/java_perf/api/records/{task_id}/"
        requests.delete(url=url, headers=self.header, verify=False)

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
        d = DevKit("172.39.173.2", "8086", "devadmin", "Huawei12#$")
        d.upload_report_by_force("/home/panlonglong/Downloads/Main(136462)")
        d.logout()
    except Exception as e:
        print(str(e))
