import logging
import multiprocessing

import constant
from handler.handler_and_node import Handler
from machine.klass_dict import KLASS_DICT

LOGGER = logging.getLogger("install_dependency")


class InstallPackage(Handler):

    def handle(self, data) -> bool:
        instruction_to_func_dict = {
            "deploy_iso": InstallPackage.deploy_iso_handle,
            "default": InstallPackage.default_handle,
        }
        return instruction_to_func_dict.get(data.get(constant.INSTRUCTION, "default"))(data)

    @staticmethod
    def deploy_iso_handle(data):
        LOGGER.debug("Deploy iso start!")
        ip_set = set()
        jobs = []

        for role in ({constant.EXECUTOR, constant.DEVKIT} & set(data.keys())):
            machine_dict = data[role + constant.MACHINE]
            LOGGER.debug(f"{role} machine list: {list(machine_dict.keys())}")
            for machine_ip in machine_dict:
                if machine_ip in ip_set:
                    continue
                ip_set.add(machine_ip)
                LOGGER.debug(f"ip_set: {ip_set}")
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
        return True

    @staticmethod
    def default_handle(data):
        LOGGER.debug("Install Package start!")
        ip_set = set()
        jobs = []

        for role in (set(KLASS_DICT.keys()) & set(data.keys())):
            machine_dict = data[role + constant.MACHINE]
            LOGGER.debug(f"{role} machine list: {list(machine_dict.keys())}")
            for machine_ip in machine_dict:
                if machine_ip in ip_set:
                    continue
                ip_set.add(machine_ip)
                LOGGER.debug(f"ip_set: {ip_set}")
                machine = machine_dict.get(machine_ip)
                process = multiprocessing.Process(
                    target=process_work,
                    args=(machine,
                          "GCCforOpenEuler",
                          "BiShengCompiler",
                          "BiShengJDK17",
                          "BiShengJDK8",
                          ),
                )
                jobs.append(process)
                process.start()

        for job in jobs:
            job.join()
        return True


def process_work(machine, *components: str):
    try:
        for component in components:
            machine.install_component(component)
    except (OSError, IOError) as e:
        LOGGER.error(f"Remote machine {machine.ip} occur Error: {str(e)}")
