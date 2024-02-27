import constant
from machine.machine import Machine


class BuilderMachine(Machine):
    def __init__(self, ip, user, pkey, password=None):
        super(BuilderMachine, self).__init__(ip, user, pkey, password)
        self.role = constant.BUILDER