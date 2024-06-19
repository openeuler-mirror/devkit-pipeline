import logging
import os.path

from devkit_utils import shell_tools


def get_docker_id(docker_path: str):
    paths_name = docker_path.strip().split("/")
    length = len(paths_name)
    if length >= 3:
        return paths_name[length - 1]
    else:
        raise Exception("can not found docker id")


def is_docker_process(pid):
    cgroup_file = f'/proc/{pid}/cgroup'
    if not os.path.exists(cgroup_file):
        return False, None
    with open(cgroup_file, "r", encoding="utf-8") as file:
        cgroup_infos = file.readlines()
        for line in cgroup_infos:
            fields = line.split(":")
            if len(fields) >= 3 and str(fields[1]) == "devices" and str(fields[2]).startswith("/docker"):
                return True, get_docker_id(fields[2])
            if len(fields) >= 3 and str(fields[1]) == "devices" and str(fields[2]).startswith("/kubepods"):
                return True, get_docker_id(fields[2])
    return False, None


def create_dir_in_docker(docker_id, target_dir, mode=755):
    outcome = shell_tools.exec_shell(f"docker exec {docker_id} mkdir -p {target_dir}", is_shell=True)
    logging.info(outcome)
    outcome = shell_tools.exec_shell(f"docker exec {docker_id} chmod {mode} {target_dir}", is_shell=True)
    logging.info(outcome)


def copy_to_docker(docker_id, origin_file, target_dir):
    file_name = os.path.basename(origin_file)
    outcome = shell_tools.exec_shell(f"docker cp {origin_file} {docker_id}:{target_dir}", is_shell=True)
    logging.info(outcome)
    outcome = shell_tools.exec_shell(f"docker exec {docker_id} chmod 755 {target_dir}/{file_name}", is_shell=True)
    logging.info(outcome)
