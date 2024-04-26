import re
import logging
import constant
from handler.handler_and_node import Handler
from constant import ROLE_LIST


LOGGER = logging.getLogger("install_dependency")
MIN_SET = (constant.USER, constant.PKEY, constant.INSTRUCTION)
MAX_SET = (constant.USER, constant.PKEY, constant.PASSWORD,
           constant.SCANNER, constant.JAVA_BUILDER_JDK8, constant.JAVA_BUILDER_JDK17,
           constant.C_BUIDLER, constant.C_BUIDLER_GCC, constant.C_BUIDLER_Bisheng_COMPILER, constant.EXECUTOR,
           constant.DEVKIT, constant.INSTRUCTION)


class BaseCheck(Handler):
    IPV4_REG = r"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"

    def handle(self, data) -> bool:
        LOGGER.debug("BaseCheck start!")
        key_set = set(data.keys())
        if not key_set.issuperset(MIN_SET) or not key_set.issubset(MAX_SET):
            LOGGER.error("Yaml file content not correct. Wrong yaml mappings.")
            return False
        
        if not BaseCheck.check_user(data):
            return False
        if not BaseCheck.check_pkey(data):
            return False
        if not BaseCheck.check_machine_ip(data):
            return False
        
        LOGGER.debug(f"After Base Check, data: {data}")
        return True
    
    @staticmethod
    def check_user(data):
        user_name = data.get(constant.USER, "")
        if not user_name:
            LOGGER.error("Yaml file content not correct. Empty user name.")
            return False
        return True
    
    @staticmethod
    def check_pkey(data):
        pkey_path = data.get(constant.PKEY, "")
        if not pkey_path:
            LOGGER.error("Yaml file content not correct. Empty pkey.")
            return False
        return True

    @staticmethod
    def check_machine_ip(data):
        for role in (ROLE_LIST & data.keys()):
            ip_list = data.get(role, [])
            if not isinstance(ip_list, list):
                LOGGER.error(f"Yaml file content not correct. Yaml file {role} value not sequence.")
                return False
            for ip in ip_list:
                if not BaseCheck.validate_ip(ip):
                    LOGGER.error(f"Yaml file content not correct. Given ip: {ip} not correct.")
                    return False
        return True
    
    @staticmethod
    def validate_ip(ip_address: str):
        return re.match(BaseCheck.IPV4_REG, ip_address)
    
