import os
import typing

from container.container import Container


def is_java_process(pid) -> bool:
    """
    判断是否是一个java进程
    """
    comm_file = f'/proc/{pid}/comm'
    if not os.path.exists(comm_file):
        return False
    with open(comm_file, "r", encoding="utf-8") as file:
        comm = file.readline()
        if "java" in comm:
            return True
    return False


def is_container_process(pid, containers: typing.List[Container]) -> (bool, Container):
    """
    判断是否是一个容器进程
    """
    cgroup_file = f'/proc/{pid}/cgroup'
    if not os.path.exists(cgroup_file):
        return False, None
    with open(cgroup_file, "r", encoding="utf-8") as file:
        cgroup_infos = file.readlines()
        for line in cgroup_infos:
            # 三层结构不支持 宿主机-containerd-containerd
            # 当前只支持两层结构 宿主机-containerd，并且宿主机 有 docker 或者（crictl和kubectl）命令
            if "devices" in line and "/docker/" in line:
                return True, __get_container(line, containers, True)
            if "devices" in line and "/kubepods/" in line:
                return True, __get_container(line, containers, False)
    return False, None


def __get_container(device_line: str, containers: typing.List[Container], is_docker: bool):
    paths_name = device_line.strip().split("/")
    length = len(paths_name)
    if length >= 3 and is_docker:
        for container in containers:
            if container.container_id in paths_name[2]:
                return container
    elif length >= 3:
        for container in containers:
            if container.container_id in paths_name[length - 1]:
                return container
    raise Exception("can not found docker id")
