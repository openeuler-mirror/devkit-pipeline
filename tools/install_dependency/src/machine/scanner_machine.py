import constant
from machine.machine import Machine


class ScannerMachine(Machine):
    def __init__(self, ip, user, pkey, password=None):
        super(ScannerMachine, self).__init__(ip, user, pkey, password)
        self.role = constant.SCANNER