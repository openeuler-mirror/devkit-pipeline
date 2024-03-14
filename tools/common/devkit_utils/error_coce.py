import enum
import typing


class ErrorCodeEnum(enum.Enum):
    SUCCESS = 0
    FAILURE = -1
    FINISHED = 1
    NOT_FOUND_JCMD = 10001
    NOT_FOUND_APPS = 10002


class ErrorCodeMsg:
    LANGUAGE_EN: typing.Dict[ErrorCodeEnum, str] = {
        ErrorCodeEnum.NOT_FOUND_JCMD: "the jcmd command was not found on server {}",
        ErrorCodeEnum.NOT_FOUND_APPS: "the application called {} was not found on server {}",
    }
