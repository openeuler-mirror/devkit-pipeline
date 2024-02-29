import logging
import multiprocessing

import constant
from handler.handler_and_node import Handler
from machine.machine import Machine
from machine.scanner_machine import ScannerMachine
from machine.builder_machine import BuilderMachine
from machine.executor_machine import ExecutorMachine

LOGGER = logging.getLogger("install_dependency")


class InstallPackage(Handler):
    klass_dict = {
        constant.SCANNER: ScannerMachine,
        constant.BUILDER: BuilderMachine,
        constant.EXECUTOR: ExecutorMachine,
    }

    def handle(self, data) -> bool:
        LOGGER.debug("Install Package start!")
        ip_set = set()
        jobs = []

        for role in InstallPackage.klass_dict:
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


def process_work(machine: Machine, *components: str):
    try:
        for component in components:
            machine.install_component(component)
    except (OSError, IOError) as e:
        LOGGER.error(f"Remote machine {machine.ip} occur Error: {str(e)}")
