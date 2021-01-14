"""
Copyright 2020 Tianshu AI Platform. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
import io
import logging
import os
import sys
import config as configs
from datetime import datetime
from logging import handlers

sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')
parser = configs.get_parser()
args = parser.parse_args()


class Logger(object):
    """
    日志配置类，定义日志格式及保存
    """
    level_mapping = {
        'debug': logging.DEBUG,
        'info': logging.INFO,
        'warning': logging.WARNING,
        'error': logging.ERROR,
        'critical': logging.CRITICAL
    }

    def __init__(self):
        if 'logs' not in os.listdir():
            os.mkdir('logs')
        date = datetime.now().strftime('%Y-%m-%d')
        file_name = 'logs/' + 'serving.log.' + date
        self.logger = logging.getLogger('serving')
        if not self.logger.handlers:
            formatter = logging.Formatter('%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s')
            self.logger.setLevel(self.level_mapping.get(args.level))  # 设置日志级别
            self.logger.propagate = False
            console_log = logging.StreamHandler()  # 往屏幕上输出
            console_log.setFormatter(formatter)  # 设置屏幕上显示的格式
            file_log = handlers.TimedRotatingFileHandler(filename=file_name, when="D", backupCount=10, encoding='utf-8')
            file_log.setFormatter(formatter)  # 设置文件里写入的格式
            self.logger.addHandler(console_log)  # 把对象加到logger里
            self.logger.addHandler(file_log)
            console_log.close()
            file_log.close()
