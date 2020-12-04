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
# coding:utf-8
import threading

import taskexecutor
import time
import common.RedisUtil as f
import common.config as config
import annotation as annotation
import luascript.starttaskscript as start_script
import logging
import common.select_gpu as gpu

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)

if __name__ == '__main__':
    """Automatic annotation algorithm entry."""
    gpu.select_gpu()
    jsonData = config.loadJsonData(config.configPath)
    redisClient = f.getRedisConnection(jsonData["ip"], jsonData["port"], jsonData["database"], jsonData["password"])
    logging.info('init redis client %s', redisClient)
    t = threading.Thread(target=taskexecutor.delayKeyThread, args=(redisClient,))
    t.setDaemon(True)
    t.start()
    annotation._init()
    while 1:
        try:
            if config.loadJsonData(config.sign) == 0:
                logging.info('not to execute new task')
                time.sleep(1)
            else:
                logging.info('get one task')
                element = redisClient.eval(start_script.startTaskLua, 1, config.queue,
                                           config.annotationStartQueue, int(time.time()))
                if len(element) > 0:
                    taskexecutor.annotationExecutor(redisClient, element[0]);
                else:
                    logging.info('task queue is empty.')
                    time.sleep(1)
        except Exception as e:
            logging.error('except:', e)
            time.sleep(1)
