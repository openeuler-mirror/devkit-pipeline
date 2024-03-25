import logging
import multiprocessing

import constant
from handler.handler_and_node import Handler
from machine.klass_dict import KLASS_DICT
from utils import available_role

LOGGER = logging.getLogger("install_dependency")


class InstallPackage(Handler):

    def handle(self, data) -> bool:
        instruction_to_func_dict = {
            "deploy_iso": InstallPackage.deploy_iso_all_handle,
            "default": InstallPackage.default_handle,
        }
        return instruction_to_func_dict.get(data.get(constant.INSTRUCTION, "default"))(data)

    @staticmethod
    def deploy_iso_all_handle(data):
        InstallPackage.deploy_iso_handle(data)
        InstallPackage.default_handle(data)
        InstallPackage.undeploy_iso_handle(data)
        return True

    @staticmethod
    def deploy_iso_handle(data):
        LOGGER.debug("Deploy iso and install Package start!")
        ip_set = set()
        jobs = []

        for role in available_role([constant.EXECUTOR, constant.DEVKIT], data):
            machine_dict = data[role + constant.MACHINE]
            LOGGER.debug(f"{role} machine list to deploy iso: {list(machine_dict.keys())}")
            for machine_ip in machine_dict:
                if machine_ip in ip_set:
                    continue
                ip_set.add(machine_ip)
                LOGGER.debug(f"ip_set to deploy iso: {ip_set}")
                machine = machine_dict.get(machine_ip)
                process = multiprocessing.Process(
                    target=process_work,
                    args=(machine,
                          "OpenEulerMirrorISO",
                          ),
                )
                jobs.append(process)
                process.start()

        for job in jobs:
            job.join()

    @staticmethod
    def default_handle(data):
        LOGGER.debug("Install Package start!")
        ip_set = set()
        jobs = []

        for role in available_role(KLASS_DICT, data):
            machine_dict = data[role + constant.MACHINE]
            LOGGER.debug(f"{role} machine list: {list(machine_dict.keys())}")
            for machine_ip in machine_dict:
                if machine_ip in ip_set:
                    continue
                ip_set.add(machine_ip)
                LOGGER.debug(f"ip_set to install package: {ip_set}")
                machine = machine_dict.get(machine_ip)
                process = multiprocessing.Process(
                    target=process_work,
                    args=(machine,
                          "GCCforOpenEuler",
                          "BiShengCompiler",
                          "BiShengJDK17",
                          "BiShengJDK8",
                          "LkpTests",
                          "NonInvasiveSwitching"
                          ),
                )
                jobs.append(process)
                process.start()

        for job in jobs:
            job.join()
        return True

    @staticmethod
    def undeploy_iso_handle(data):
        ip_set = set()
        jobs = []

        for role in available_role([constant.EXECUTOR, constant.DEVKIT], data):
            machine_dict = data[role + constant.MACHINE]
            LOGGER.debug(f"{role} machine list to un-deploy iso: {list(machine_dict.keys())}")
            for machine_ip in machine_dict:
                if machine_ip in ip_set:
                    continue
                ip_set.add(machine_ip)
                LOGGER.debug(f"ip_set to un-deploy iso: {ip_set}")
                machine = machine_dict.get(machine_ip)
                process = multiprocessing.Process(
                    target=process_work,
                    args=(machine,
                          "UnOpenEulerMirrorISO",
                          ),
                )
                jobs.append(process)
                process.start()

        for job in jobs:
            job.join()


def process_work(machine, *components: str):
    try:
        for component in components:
            machine.install_component(component)
    except (OSError, IOError) as e:
        LOGGER.error(f"Remote machine {machine.ip} occur Error: {str(e)}")
