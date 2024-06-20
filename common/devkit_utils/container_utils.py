import typing

from container.container import Container
from container.crictl import CrictlContainerFactory
from container.docker import DockerContainerFactory


def get_containers() -> typing.List[Container]:
    """
    获取容器信息，当存在docker命令时，使用docker命令获取，不存在docker命令时，尝试使用crictl（必须存在kubectl命令）获取
    """
    docker_factory = DockerContainerFactory()
    if docker_factory.check_in_machine():
        return docker_factory.get_container()
    crictl_factory = CrictlContainerFactory()
    if crictl_factory.check_in_machine():
        return crictl_factory.get_container()
    return list()
