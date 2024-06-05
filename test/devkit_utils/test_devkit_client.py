import unittest
from unittest import TestCase

from devkit_utils.devkit_client import DevKitClient


class TestDevKitClient(TestCase):

    @unittest.skip("跳过这个测试方法")
    def test_login(self):
        try:
            client = DevKitClient("172.39.173.2", "8086", "devadmin", "Huawei12#$")
            client.upload_report_by_force("/home/panlonglong/Downloads/Main(136462)")
            client.logout()
        except Exception as e:
            print(str(e))
        self.fail()

    @unittest.skip("跳过这个测试方法")
    def test_login_use_proxy(self):
        try:
            client = DevKitClient("172.39.173.2", "8086", "devadmin", "Huawei12#$")
            client.use_proxy = False
            client.upload_report_by_force("/home/panlonglong/Downloads/Main(136462)")
            client.logout()
        except Exception as e:
            print(str(e))
        self.fail()

    def test_try_to_login(self):
        self.fail()

    def test_logout(self):
        self.fail()

    def test_upload_report_by_force(self):
        self.fail()
