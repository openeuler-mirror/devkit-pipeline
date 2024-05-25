import logging
import socket

import constant
from constant import ROLE_COMPONENT, ROLE_LIST
from deploy.a_fot_deploy import AFotDeploy
from deploy.bisheng_compiler_deploy import BiShengCompilerDeploy
from deploy.bisheng_jdk17_deploy import BiShengJDK17Deploy
from deploy.bisheng_jdk8_deploy import BiShengJDK8Deploy
from deploy.compatibility_test_deploy import CompatibilityDeploy
from deploy.devkit_distribute_deploy import DevkitTesterDeploy
from deploy.devkitcli_deploy import DevkitCLIDeploy
from deploy.devkitweb_deploy import DevkitWebDeploy
from deploy.gcc_deploy import GccDeploy
from deploy.lkptest_deploy import LkpTestDeploy
from deploy.mount_mirror_deploy import MountISODeploy
from deploy.switch_deploy import SwitchDeploy
from deploy.unmount_mirror_deploy import UnMountISODeploy
from exception.connect_exception import ConnectException
from handler.handler_and_node import Handler
from utils import global_value

LOGGER = logging.getLogger("install_dependency")
ROLE_MAP = {
    "BiShengJDK17": BiShengJDK17Deploy,
    "BiShengJDK8": BiShengJDK8Deploy,
    "DevKitCLI": DevkitCLIDeploy,
    "GCCforOpenEuler": GccDeploy,
    "BiShengCompiler": BiShengCompilerDeploy,
    "A-FOT": AFotDeploy,
    "NonInvasiveSwitching": SwitchDeploy,
    "LkpTests": LkpTestDeploy,
    "CompatibilityTesting": CompatibilityDeploy,
    "DevkitTester": DevkitTesterDeploy,
    "DevKitWeb": DevkitWebDeploy,
    "OpenEulerMirrorISO": MountISODeploy,
    "UnOpenEulerMirrorISO": UnMountISODeploy
}


class ConnectCheck(Handler):
    def handle(self, data) -> bool:
        LOGGER.debug("ConnectCheck start!")
        data[constant.MACHINE] = dict()
        ret = True
        for role in (ROLE_LIST & data.keys()):
            ret = ret and ConnectCheck.deploy_role_check(data, role)
        return ret

    @staticmethod
    def deploy_role_check(data, role):
        builder_list = data.get(role)
        if data.get(constant.INSTRUCTION) == "deploy_iso":
            global_value["DEPLOY_ISO"] = True
        for ip in builder_list:
            try:
                for each in ROLE_COMPONENT[role]:
                    if ip in global_value["ip"].keys():
                        global_value["ip"][ip]["component_set"].add(each)
                    else:
                        global_value["ip"][ip] = dict()
                        global_value["ip"][ip]["component_set"] = set()
                        global_value["ip"][ip]["component_set"].add(each)
                global_value["ip"][ip]["component_list"] = list(global_value["ip"][ip]["component_set"])
                if global_value["DEPLOY_ISO"]:
                    global_value["ip"][ip]["component_list"].insert(0, "OpenEulerMirrorISO")
                    global_value["ip"][ip]["component_list"].append("UnOpenEulerMirrorISO")
                global_value["ip"][ip]["deploy_component"] = []
                for each in global_value["ip"][ip]["component_list"]:
                    global_value["ip"][ip]["deploy_component"].append(ROLE_MAP.get(each)(each, ip, data[constant.USER],
                                                                                         data[constant.PKEY],
                                                                                         data.get(constant.PASSWORD)))
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
