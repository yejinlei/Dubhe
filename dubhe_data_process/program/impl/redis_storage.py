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
import json
import uuid
from abc import ABC
from program.abstract.storage import Storage
import common.util.public.RedisUtil as f
import common.config.config as config
import logging
import time
from program.thread.delay_schedule import Redis_thread

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)


class RedisStorage(Storage, ABC):
    """
    基于redis实现的任务存储
    """

    def init_client(path):
        """
            init method
            """
        json_data = config.loadJsonData(path)
        redis_client = f.getRedisConnection(json_data["ip"], json_data["port"], json_data["database"],
                                            json_data["password"])
        logging.info("redis client init success %s", redis_client)
        return redis_client
        pass

    def get_one_task(*args):
        logging.debug("Parameter: %s", args)
        time.sleep(1)
        redis_client = RedisStorage.init_client(args[4])
        task_id = redis_client.eval(args[0], args[1], args[2], args[3], int(time.time()))
        if len(task_id) > 0 and task_id[0] is not None:
            Redis_thread.redis_client_thread = redis_client
            Redis_thread.processing_key = task_id[0].decode()
            logging.info("------------processing_key = %s------------------", Redis_thread.processing_key)
            return task_id[0].decode(), json.loads((redis_client.get(task_id[0].decode().replace('"', ''))).decode())
        return 0

    def save_result(*args):
        """
        Save the results
        """
        redis_client = RedisStorage.init_client(args[2])
        uuid_key = str(uuid.uuid1())
        uuid_detail_key = "\"" + uuid_key + "\""
        if args[4] is True:
            redis_client.zrem(Redis_thread.processing_queue, Redis_thread.processing_key)
            redis_client.set(uuid_key, json.dumps(args[3]))
            f.pushToQueue(redis_client, args[0], uuid_detail_key)
        else:
            redis_client.zrem(Redis_thread.processing_queue, Redis_thread.processing_key)
            redis_client.set(uuid_key, json.dumps(args[3]))
            f.pushToQueue(redis_client, args[1], uuid_detail_key)
