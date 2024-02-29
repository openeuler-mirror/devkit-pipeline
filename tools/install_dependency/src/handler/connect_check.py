import logging
import constant
from handler.handler_and_node import Handler
from machine.scanner_machine import ScannerMachine
from machine.builder_machine import BuilderMachine
from machine.executor_machine import ExecutorMachine
from exception.connect_exception import ConnectException

LOGGER = logging.getLogger("install_dependency")


class ConnectCheck(Handler):
    klass_dict = {
        constant.SCANNER: ScannerMachine,
        constant.BUILDER: BuilderMachine,
        constant.EXECUTOR: ExecutorMachine,
    }

    def handle(self, data) -> bool:
        LOGGER.debug("ConnectCheck start!")
        ret = True
        for role in ConnectCheck.klass_dict:
            ret = ret and ConnectCheck.machine_role_check(data, role)
        return ret
    
    @staticmethod
    def machine_role_check(data, role):
        builder_list = data.get(role)
        klass = ConnectCheck.klass_dict.get(role)
        data[role + constant.MACHINE] = dict()
        for ip in builder_list:
            try:
                machine_instance = klass(ip, data[constant.USER], data[constant.PKEY],
                                         data.get(constant.PASSWORD, None))
                data[role + constant.MACHINE][ip] = machine_instance
            except ConnectException:
                LOGGER.error(f"-- [error] Connect {ip} failed. Please check.")
                del data[role + constant.MACHINE]
                return False
            except Exception as e:
                LOGGER.error(f"-- [error] Connect {ip} failed. Because of {str(e)}")
                del data[role + constant.MACHINE]
                return False
        return True
