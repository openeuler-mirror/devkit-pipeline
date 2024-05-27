import logging
import socket

import constant
from handler.handler_and_node import Handler
from machine.machine import Machine
from exception.connect_exception import ConnectException
from constant import ROLE_COMPONENT, ROLE_LIST

LOGGER = logging.getLogger("deploy_tool")


class ConnectCheck(Handler):
    def handle(self, data) -> bool:
        LOGGER.debug("ConnectCheck start!")
        data[constant.MACHINE] = dict()
        ret = True
        for role in (ROLE_LIST & data.keys()):
            ret = ret and ConnectCheck.machine_connect_check(data, role)
        return ret

    @staticmethod
    def machine_connect_check(data, role):
        machine_list = data.get(role)
        for ip in machine_list:
            try:
                machine_instance = data[constant.MACHINE].get(ip, Machine(ip, data[constant.USER], data[constant.PKEY],
                                                                          data.get(constant.PASSWORD, None)))
                machine_instance.add_component(ROLE_COMPONENT[role])
                data[constant.MACHINE][ip] = machine_instance
            except ConnectException:
                LOGGER.error(f"-- [error] Connect {ip} failed. Please check.")
                del data[constant.MACHINE]
                return False
            except Exception as e:
                LOGGER.error(f"-- [error] Connect {ip} failed. Because of {str(e)}")
                del data[constant.MACHINE]
                return False
        return True

    @staticmethod
    def get_local_ip():
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            sock.connect(("8.8.8.8", 80))
            ip = sock.getsockname()[0]
        finally:
            sock.close()
        return ip if ip else "127.0.0.1"
