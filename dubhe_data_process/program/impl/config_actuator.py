# !/usr/bin/env python
# -*- coding:utf-8 -*-

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
=============================================================
"""

import os
from abc import ABC
import logging
from program.abstract.actuator import Actuator
from common.util.public.json_util import JsonUtil
import importlib


class ConfigActuator(Actuator, ABC):
    def execute(self, algorithm):
        """
        Actuator method
        """
        algorithm_config_json = '/program/exec/' + algorithm + '/config.json'
        algorithm_config = JsonUtil.load_json(os.path.abspath('.' + algorithm_config_json))
        keys = list(algorithm_config.keys())
        use_config = 'base'
        if algorithm in keys:
            use_config = algorithm
        steps = list(algorithm_config[use_config])
        now_step = 0
        step_result = [None] * len(steps)
        while now_step != -1:
            item = steps[now_step]
            logging.info("本次参数 now-step[%s]", now_step)
            item_keys = list(item.keys())
            module = importlib.import_module(str(item['module']))
            classs = getattr(module, item['class'])
            param = []
            if 'paramLocal' in item_keys:
                for p in item['paramLocal']:
                    param.append(p)

            if item['paramType'] == 1:
                if 'param' in item_keys:
                    for p in item['param']:
                        param.append(step_result[int(str(p).split(".")[0]) - 1][int(str(p).split(".")[1]) - 1])
            result = getattr(classs, item['method'])(*param)
            print(len(step_result))
            step_result[now_step] = result
            logging.debug('step %s result %s', now_step, result)
            if 'judge' in item_keys:
                if item['judge'] == result:
                    print('需要跳转')
                    now_step = item['jump'] - 1
                    print(now_step)
                else:
                    now_step = now_step + 1
            elif 'jump' in item_keys:
                now_step = item['jump'] - 1
                print(now_step)
            else:
                now_step = now_step + 1
