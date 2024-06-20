import logging
import os.path
import typing

from container.container import Container, ContainerFactory
from devkit_utils import shell_tools


class DockerContainer(Container):

    def __init__(self, container_id):
        super().__init__(container_id)

    def create_dir_in_container(self, target_dir, mode=755):
        outcome = shell_tools.exec_shell(f"docker exec {self.container_id} mkdir -p {target_dir}", is_shell=True)
        logging.info(outcome)
        outcome = shell_tools.exec_shell(f"docker exec {self.container_id} chmod {mode} {target_dir}", is_shell=True)
        logging.info(outcome)

    def copy_to_container(self, origin_file, target_dir, mode=755):
        file_name = os.path.basename(origin_file)
        outcome = shell_tools.exec_shell(f"docker cp {origin_file} {self.container_id}:{target_dir}", is_shell=True)
        logging.info(outcome)
        outcome = shell_tools.exec_shell(f"docker exec {self.container_id} chmod {mode} {target_dir}/{file_name}",
                                         is_shell=True)
        logging.info(outcome)

    def copy_to_host(self, origin_file, target_dir, mode=755):
        outcome = shell_tools.exec_shell(f"docker cp {self.container_id}:{origin_file} {target_dir}",
                                         is_shell=True)
        logging.info(outcome)

    def delete_in_container(self, target_dir):
        outcome = shell_tools.exec_shell(f"docker exec {self.container_id} rm -rf {target_dir}",
                                         is_shell=True)
        logging.info(outcome)


class DockerContainerFactory(ContainerFactory):
    def __init__(self):
        self.container_id_index = 0

    def check_in_machine(self):
        outcome = shell_tools.exec_shell("docker ps", is_shell=True)
        logging.info(outcome)
        if outcome.return_code == 0:
            return True
        else:
            return False

    def get_container(self) -> typing.List[Container]:
        containers = list()
        outcome = shell_tools.exec_shell("docker ps", is_shell=True)
        lines = outcome.out.split("\n")
        for line in lines[1:]:
            fields = line.split()
            containers.append(DockerContainer(fields[self.container_id_index]))
        return containers
