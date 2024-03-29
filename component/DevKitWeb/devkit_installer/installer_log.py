import logging
import sys


def config_logging(debug=False):
    logger = logging.getLogger("devkit_installer")
    logger.setLevel(logging.DEBUG)

    formatter = logging.Formatter(
        "[%(asctime)s] [%(levelname)s] [processID:%(process)d]"
        " [%(threadName)s] [%(module)s:%(funcName)s:%(lineno)d]"
        " %(message)s")

    handler = logging.StreamHandler(sys.stdout)
    handler.setLevel(logging.DEBUG if debug else logging.INFO)
    handler.setFormatter(formatter)

    logger.addHandler(handler)
