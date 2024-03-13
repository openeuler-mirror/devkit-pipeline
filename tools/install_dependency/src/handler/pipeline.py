import logging

from handler.handler_and_node import Node

LOGGER = logging.getLogger("install_dependency")


class PipeLine:
    """维护一个链表"""

    def __init__(self, data):
        self.head: Node = Node()
        self.tail: Node = self.head
        self.data = data

    def start(self):
        if self.head.get_next_node() and self.head.get_next_node().execute(self.data):
            print("-- Program finished. --")

    def add_tail(self, *handlers):
        for handler in handlers:
            node = Node(handler)
            self.tail.set_next_node(node)
            self.tail = node
