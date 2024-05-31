import logging
import sys


def config_logging(silent=False):
    logger = logging.getLogger("deploy_tool")
    logger.setLevel(logging.DEBUG)

    formatter = logging.Formatter(
        "[%(asctime)s] [%(levelname)s] [processID:%(process)d]"
        " [%(threadName)s] [%(module)s:%(funcName)s:%(lineno)d]"
        " %(message)s")

    handler = logging.StreamHandler(sys.stdout)
    handler.setLevel(logging.INFO if silent else logging.DEBUG)
    handler.setFormatter(formatter)

    logger.addHandler(handler)
