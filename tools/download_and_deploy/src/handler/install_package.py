import logging
import multiprocessing

import constant
from handler.handler_and_node import Handler

LOGGER = logging.getLogger("deploy_tool")


class InstallPackage(Handler):

    def handle(self, data):
        LOGGER.debug("Install Package start!")

        queue = multiprocessing.Queue()
        jobs = []
        for _, machine in data[constant.MACHINE].items():
            process = multiprocessing.Process(target=process_work, args=(queue, machine,))
            jobs.append(process)
            process.start()
        for job in jobs:
            job.join()

        while not queue.empty():
            modified_machine_instance = queue.get()
            machine_instance = data[constant.MACHINE].get(modified_machine_instance.ip)
            machine_instance.component_dict = modified_machine_instance.component_dict
        return True


def process_work(queue, machine):
    try:
        machine.install_components()
        queue.put(machine)
    except (OSError, IOError) as e:
        LOGGER.error(f"Remote machine {machine.ip} occur Error: {str(e)}")
