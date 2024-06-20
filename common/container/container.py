import abc
import typing


class Container(abc.ABC):
    def __init__(self, container_id):
        self.container_id = container_id

    @abc.abstractmethod
    def create_dir_in_container(self, target_dir, mode=755):
        """
        在docker中创建目录
        """

    @abc.abstractmethod
    def copy_to_container(self, origin_file, target_dir, mode=755):
        """
        复制宿主机文件到容器内
        """

    @abc.abstractmethod
    def copy_to_host(self, origin_file, target_dir, mode=755):
        """
        复制容器中的文件到宿主机
        """

    @abc.abstractmethod
    def delete_in_container(self, target_dir):
        """
        删除文件夹
        """


class ContainerFactory(abc.ABC):

    @abc.abstractmethod
    def check_in_machine(self):
        """
        检测当前服务器中是否存在该类型容器
        """

    @abc.abstractmethod
    def get_container(self) -> typing.Dict[str, Container]:
        """
        获取当前运行的容器
        """
