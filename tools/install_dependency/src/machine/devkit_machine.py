import constant
from machine.machine import Machine


class DevkitMachine(Machine):
    def __init__(self, ip, user, pkey, password=None):
        super(DevkitMachine, self).__init__(ip, user, pkey, password)
        self.role = constant.DEVKIT
