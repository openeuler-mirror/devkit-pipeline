class ConnectException(Exception):
    def __init__(self):
        super(ConnectException, self).__init__()
        self.status = ""
        self.value = ""


class ConnectRemoteException(ConnectException):
    def __init__(self):
        super(ConnectRemoteException, self).__init__()


class CreatePkeyFailedException(ConnectException):
    def __init__(self):
        super(CreatePkeyFailedException, self).__init__()


class NotMatchedMachineTypeException(ConnectException):
    def __init__(self):
        super(NotMatchedMachineTypeException, self).__init__()
