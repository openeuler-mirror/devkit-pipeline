from abc import abstractmethod


class Handler:
    """处理器基类"""

    def __init__(self):
        pass

    @abstractmethod
    def handle(self, data) -> bool:
        pass


class Node:
    """链表节点"""

    def __init__(self, handler=None):
        self.handler: Handler = handler
        self.next_node: Node = None

    def get_next_node(self):
        return self.next_node

    def set_next_node(self, node):
        self.next_node = node

    def execute(self, data):
        ret: bool = self.handler.handle(data)
        if not ret:
            return False
        if self.next_node:
            return self.next_node.execute(data)
        return True
