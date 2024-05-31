import logging

import constant
from handler.handler_and_node import Handler
from machine.machine import Machine

LOGGER = logging.getLogger("deploy_tool")


class ResultReport(Handler):
    def handle(self, data) -> bool:
        LOGGER.debug("Result report!")
        machine_dict = data[constant.MACHINE]

        for ip in machine_dict:
            machine_instance = machine_dict.get(ip)
            ResultReport.report_machine_deploy_result(machine_instance)
        return True

    @staticmethod
    def report_machine_deploy_result(machine_instance):
        report_str = f"Deploy result follow. \nRemote machine {machine_instance.ip} deploy result: \n"
        component_dict = machine_instance.component_dict

        for component_name in component_dict:
            deploy_result = component_dict[component_name]
            if deploy_result:
                report_str += f"\t{component_name} {deploy_result}\n"
            else:
                report_str += f"\t{component_name} install failed.\n"
        LOGGER.info(f"{report_str}")

