import logging
import os.path
import typing

from container.container import Container, ContainerFactory
from devkit_utils import shell_tools


class CrictlContainer(Container):

    def __init__(self, container_id, name, pod_id, pod):
        super().__init__(container_id)
        self.name = name
        self.pod_id = pod_id
        self.pod = pod

    def create_dir_in_container(self, target_dir, mode=755):
        outcome = shell_tools.exec_shell(f"kubectl exec {self.pod} -c {self.name} -- mkdir -p {target_dir}",
                                         is_shell=True)
        logging.info(outcome)
        outcome = shell_tools.exec_shell(f"kubectl exec {self.pod} -c {self.name} -- chmod {mode} {target_dir}",
                                         is_shell=True)
        logging.info(outcome)

    def copy_to_container(self, origin_file, target_dir, mode=755):
        file_name = os.path.basename(origin_file)
        outcome = shell_tools.exec_shell(f"kubectl cp -c {self.name} {origin_file} {self.pod}:{target_dir}",
                                         is_shell=True)
        logging.info(outcome)
        outcome = shell_tools.exec_shell(
            f"kubectl exec {self.pod} -c {self.name} -- chmod {mode} {target_dir}/{file_name}",
            is_shell=True)
        logging.info(outcome)

    def copy_to_host(self, origin_file, target_dir, mode=755):
        outcome = shell_tools.exec_shell(f"kubectl cp -c {self.name} {self.pod}:{origin_file} {target_dir}",
                                         is_shell=True)
        logging.info(outcome)

    def delete_in_container(self, target_dir):
        outcome = shell_tools.exec_shell(f"kubectl exec {self.pod} -c {self.name} -- rm -rf {target_dir}",
                                         is_shell=True)
        logging.info(outcome)


class CrictlContainerFactory(ContainerFactory):

    def __init__(self):
        self.container_id_index = 0
        self.name_index = 0
        self.pod_id_index = 0
        self.pod_index = 0

    def check_in_machine(self):
        outcome = shell_tools.exec_shell("crictl ps", is_shell=True)
        logging.info(outcome)
        if outcome.return_code == 0:
            outcome = shell_tools.exec_shell("kubectl get pods", is_shell=True)
            if outcome.return_code == 0:
                return True
        return False

    def get_container(self) -> typing.List[Container]:
        containers = list()
        outcome = shell_tools.exec_shell("crictl ps", is_shell=True)
        lines = outcome.out.split("\n")
        self.__parse(lines[0])
        for line in lines[1:]:
            fields = line.split()
            containers.append(CrictlContainer(fields[self.container_id_index], fields[self.name_index],
                                              fields[self.pod_id_index], fields[self.pod_index]))
        return containers

    def __parse(self, header: str):
        fields = header.split()
        for i in range(0, len(fields)):
            field = header[i].lower()
            if field == "container":
                self.container_id_index = i
            if field == "name":
                self.name_index = i
            if field == "pod_id":
                self.pod_id_index = i
            if field == "pod":
                self.pod_index = i
