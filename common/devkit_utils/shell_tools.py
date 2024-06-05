import logging
import shlex
import subprocess
import typing

from devkit_utils.pyinstaller_utils import PyInstallerUtils


class ExecutionOutcome:
    def __init__(self, return_code=0, out=None, err=None):
        self.return_code: int = return_code
        self.out: typing.Optional[str] = out
        self.err: typing.Optional[str] = err

    def __str__(self):
        return "code:%s\n out:%s\n err:%s\n" % (self.return_code, self.out, self.err)


def exec_shell(command: str, is_shell=False, timeout=30, env=PyInstallerUtils.get_env()) -> ExecutionOutcome:
    """
    执行命令，返回执行结果
    """
    try:
        if is_shell:
            child = subprocess.Popen(command, close_fds=True, stdout=subprocess.PIPE, stdin=None, env=env,
                                     stderr=subprocess.PIPE, encoding="utf-8", universal_newlines=True, shell=is_shell)

        else:
            child = subprocess.Popen(shlex.split(command), close_fds=True, stdout=subprocess.PIPE, stdin=None, env=env,
                                     stderr=subprocess.PIPE, encoding="utf-8", universal_newlines=True)

    except Exception as ex:
        logging.error(ex, exc_info=True)
        raise ex
    else:
        try:
            out, err = child.communicate(timeout=timeout)
        except Exception as exception:
            logging.error(exception, exc_info=True)
            child.kill()
            raise exception
        else:
            return ExecutionOutcome(child.returncode, out, err)
