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
import threading
import time

import common.RedisUtil as f
import luascript.starttaskscript as start_script
import common.config as config
import logging
import imgprocess

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)

if __name__ == '__main__':
    """Enhancement algorithm entry."""
    jsonData = config.loadJsonData(config.configPath)
    redisClient = f.getRedisConnection(jsonData["ip"], jsonData["port"], jsonData["database"], jsonData["password"])
    logging.info('init redis client %s', redisClient)
    t = threading.Thread(target=imgprocess.delayKeyThread, args=(redisClient,))
    t.setDaemon(True)
    t.start()
    while 1:
        try:
            if config.loadJsonData(config.sign) == 0:
                logging.info('not to execute new task')
                time.sleep(5)
            else:
                enhanceTaskId = redisClient.eval(start_script.startTaskLua, 1, config.imgProcessTaskQueue,
                                                 config.imgProcessStartQueue, int(time.time()))
                if len(enhanceTaskId) > 0:
                    imgprocess.start_enhance_task(enhanceTaskId, redisClient)
                else:
                    logging.info('task queue is empty.')
                    time.sleep(5)
        except Exception as e:
            logging.error('except:', e)
            time.sleep(1)
