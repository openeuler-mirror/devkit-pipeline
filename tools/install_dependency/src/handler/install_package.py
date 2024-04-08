import logging
import multiprocessing

import constant
from utils import global_value
from handler.handler_and_node import Handler

LOGGER = logging.getLogger("install_dependency")


class InstallPackage(Handler):

    def handle(self, data):
        LOGGER.debug("Install Package start!")
        jobs = []
        undeploy_iso = None
        for each in global_value["ip"].keys():
            process = multiprocessing.Process(target=process_deploy_component, args=(each,))
            jobs.append(process)
            process.start()
            for job in jobs:
                job.join()
        return True


def process_deploy_component(ip):
    try:
        undeploy_iso = None
        if global_value["DEPLOY_ISO"]:
            global_value["ip"][ip]["deploy_component"][0].install_component_handler()
            undeploy_iso = global_value["ip"][ip]["deploy_component"][-1]
            del global_value["ip"][ip]["deploy_component"][0]
            del global_value["ip"][ip]["deploy_component"][-1]
        for every in global_value["ip"][ip]["deploy_component"]:
            every.install_component_handler()
        if global_value["DEPLOY_ISO"]:
            undeploy_iso.install_component_handler()
    except (OSError, IOError) as e:
        LOGGER.error(f"Remote machine {ip} occur Error: {str(e)}")

