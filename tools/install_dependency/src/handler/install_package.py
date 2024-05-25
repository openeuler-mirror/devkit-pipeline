import logging
import multiprocessing

import constant
from handler.handler_and_node import Handler

LOGGER = logging.getLogger("install_dependency")


class InstallPackage(Handler):

    def handle(self, data):
        LOGGER.debug("Install Package start!")
        jobs = []
        for _, machine in data[constant.MACHINE].items():
            process = multiprocessing.Process(target=process_work, args=(machine,))
            jobs.append(process)
            process.start()
        for job in jobs:
            job.join()
        return True


def process_work(machine):
    try:
        machine.install_components()
    except (OSError, IOError) as e:
        LOGGER.error(f"Remote machine {machine.ip} occur Error: {str(e)}")
