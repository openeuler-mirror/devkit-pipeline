import constant
from machine.machine import Machine


class DevkitMachine(Machine):
    def __init__(self, ip, user, pkey, password=None):
        super(DevkitMachine, self).__init__(ip, user, pkey, password)
        self.role = constant.DEVKIT

    def install_component_handler(self, component_name, sftp_client, ssh_client):
        component_name_to_func_dict = {
            "GCCforOpenEuler": self.default_install_component_handle,
            "BiShengCompiler": self.default_install_component_handle,
            "BiShengJDK17": self.default_install_component_handle,
            "BiShengJDK8": self.default_install_component_handle,
            "LkpTests": self.do_nothing,
            "OpenEulerMirrorISO": self.deploy_iso_handle,
        }
        return component_name_to_func_dict.get(component_name)(component_name, sftp_client, ssh_client)
