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
import json
import threading
from datetime import datetime
import time
import sys
sys.path.append("../")
import common.RedisUtil as f
import luascript.starttaskscript as start_script
import common.config as config
import logging
from entrance.executor import lungsegmentation as lungseg
import redis

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s', level=logging.DEBUG)

if __name__ == '__main__':
    """Lung segmentation algorithm based on CT image dcm entry."""
    jsonData = config.loadJsonData(config.configPath)
    redisClient = f.getRedisConnection(jsonData["ip"], jsonData["port"], jsonData["database"], jsonData["password"])
    logging.info('init redis client %s', redisClient)
    t = threading.Thread(target=lungseg.delayKeyThread, args=(redisClient,))
    t.setDaemon(True)
    t.start()
    while 1:
        try:
            # if config.loadJsonData(config.sign) == 0:
            #     logging.info('not to execute new task')
            #     time.sleep(5)
            # else:
            logging.info("read redis:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S"))
            lungTask = redisClient.eval(start_script.startTaskLua, 1, config.dcmTaskQueue, config.dcmStartQueue, int(time.time()))
            if len(lungTask) > 0:
                logging.info("start process.")
                key = lungTask[0].decode()
                jsonStr = f.getByKey(redisClient, key.replace('"', ''))
                if lungseg.process(jsonStr, lungTask[0]):
                    f.pushToQueue(redisClient, config.dcmFinishQueue, key)
                    redisClient.zrem(config.dcmStartQueue, lungTask[0])
                logging.info('success.')
            else:
                logging.info('task queue is empty.')
                time.sleep(1)
        except Exception as e:
            logging.error('except:', e)
            time.sleep(1)
