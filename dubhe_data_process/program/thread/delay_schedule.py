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

import time
import sched
import threading
import logging
import script.delaytaskscript as delay_script
from datetime import datetime

schedule = sched.scheduler(time.time, time.sleep)


class Start_thread:
    def start_thread(self):
        Redis_thread.processing_queue = self
        t = threading.Thread(target=delay_key_thread, args=())
        t.setDaemon(True)
        t.start()


class Redis_thread:
    processing_queue = None
    redis_client_thread = None
    processing_key = None


def delay_schedule(inc, ):
    """Delay task method.
        Args:
            self: scheduled task time.
            :param inc:
    """
    try:
        logging.info("delay:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S"))
        if Redis_thread.processing_key is not None:
            logging.info("执行一次delay操作")
            logging.info("---------------delay_key = %s", Redis_thread.processing_key)
            Redis_thread.redis_client_thread.eval(delay_script.delayTaskLua, 1, Redis_thread.processing_queue,
                                                  Redis_thread.processing_key,
                                                  int(time.time()))
        schedule.enter(inc, 0, delay_schedule, (inc,))
    except Exception as e:
        logging.info("delay error: %s", e)


def delay_key_thread():
    """Delay task thread.
        Args:
    """
    schedule.enter(0, 0, delay_schedule, (5,))
    schedule.run()
