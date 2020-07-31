"""
/**
* Copyright 2020 Zhejiang Lab. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* =============================================================
*/
"""

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import logging
import re
from logging.handlers import TimedRotatingFileHandler


def setup_log(mode, log_name):
    logger = logging.getLogger(log_name)
    log_name = os.path.join('./log', mode, log_name)
    logger.setLevel(logging.DEBUG)

    file_handler = TimedRotatingFileHandler(
        filename=log_name, when="MIDNIGHT", interval=1, backupCount=7
    )
    stream_handler = logging.StreamHandler()
    stream_handler.setLevel(logging.DEBUG)

    file_handler.suffix = "%Y-%m-%d.log"
    file_handler.extMatch = re.compile(r"^\d{4}-\d{2}-\d{2}.log$")
    #
    file_handler.setFormatter(
        logging.Formatter(
            "[%(asctime)s][%(levelname)s] - %(message)s"
        )
    )
    stream_handler.setFormatter(
        logging.Formatter(
            "[%(asctime)s][%(levelname)s] - %(message)s"
        )
    )
    logger.addHandler(file_handler)
    logger.addHandler(stream_handler)
    return logger
