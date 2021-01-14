"""
/**
* Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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
# coding:utf-8

import os
import json
import threading
import time
import sys
sys.path.append("../")
import common.RedisUtil as f
import common.config as config
import luascript.starttaskscript as start_script
import logging
import traceback
from entrance.executor import ofrecord

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',level=logging.DEBUG)

basePath = '/nfs/'
descPath = 'ofrecord/train'

if __name__ == '__main__':
    """Ofrecord algorithm entry."""
    jsonData = config.loadJsonData(config.configPath)
    redisClient = f.getRedisConnection(jsonData["ip"], jsonData["port"], jsonData["database"], jsonData["password"])
    logging.info('init redis client %s', redisClient)
    t = threading.Thread(target=ofrecord.delayKeyThread, args=(redisClient,))
    t.setDaemon(True)
    t.start()
    while 1:
        try:
            if config.loadJsonData(config.sign) == 0:
                logging.info('not to execute new task')
                time.sleep(1)
            else:
                element = redisClient.eval(start_script.startTaskLua, 1, config.ofrecordTaskQueue,
                                           config.ofrecordStartQueue, int(time.time()))
                if len(element) > 0:
                    key = element[0].decode()
                    detail = f.getByKey(redisClient, key.replace('"', ''))
                    jsonStr = json.loads(detail.decode())
                    label_map = {}
                    index = 0
                    for item in jsonStr["datasetLabels"].keys():
                        if index >= 0 and item != '@type':
                            label_map[item] = jsonStr["datasetLabels"][item]
                        index += 1
                    ofrecord.execute(os.path.join(basePath, jsonStr["datasetPath"]),
                                     os.path.join(basePath, jsonStr["datasetPath"], descPath),
                                     label_map,
                                     jsonStr["files"],
                                     jsonStr["partNum"],
                                     element[0])
                    logging.info('save result to redis')
                    f.pushToQueue(redisClient, config.ofrecordFinishQueue, key)
                    redisClient.zrem(config.ofrecordStartQueue, element[0])
                else:
                    logging.info('task queue is empty.')
                    time.sleep(2)
        except Exception as e:
            logging.error('except:', e)
            redisClient.zrem(config.ofrecordStartQueue, element[0])
            time.sleep(1)
