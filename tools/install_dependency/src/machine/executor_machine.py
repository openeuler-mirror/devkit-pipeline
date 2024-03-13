import constant
from machine.machine import Machine


class ExecutorMachine(Machine):
    def __init__(self, ip, user, pkey, password=None):
        super(ExecutorMachine, self).__init__(ip, user, pkey, password)
        self.role = constant.EXECUTOR
