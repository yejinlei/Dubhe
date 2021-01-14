import sys
import logging
import multiprocessing

def setup_logger(name):
  # Manually clear root loggers to prevent any module that may have called
  # logging.basicConfig() from blocking our logging setup
  # logging.root.handlers = []
  FORMAT = '%(asctime)s [%(levelname)s] %(filename)s:%(lineno)d: %(message)s'
  # DATEFMT = '%Y-%m-%d,%H:%M:%S.%f'
  logging.basicConfig(level=logging.INFO, format=FORMAT, stream=sys.stdout)
  logger = logging.getLogger(name)
  return logger

def setup_multiprocessing_logger():
  logger = multiprocessing.get_logger()
  logger.setLevel(logging.INFO)
  if not logger.handlers:
    logger._rudimentary_setup = True
    logfile = sys.__stdout__
    if hasattr(logfile, "write"):
        handler = logging.StreamHandler(logfile)
    else:
        handler = logging.FileHandler(logfile)
    # formatter = logging.Formatter('%(asctime)s [%(levelname)s] [%(processName)s] '
    #                               '%(filename)s:%(lineno)d: %(message)s')
    formatter = logging.Formatter('%(asctime)s [%(levelname)s] [%(processName)s] %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)

  return logger