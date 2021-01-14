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

import codecs
import sched
import sys
import json
import logging
import time
import common.RedisUtil as f
import common.config as config
from entrance.executor import text_classification as text_classification
from datetime import datetime
import luascript.delaytaskscript as delay_script

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)

schedule = sched.scheduler(time.time, time.sleep)
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

delayId = ""


def textClassificationExecutor(redisClient, key):
    """Annotation task method.
                Args:
                  redisClient: redis client.
                  key: annotation task key.
    """
    global delayId
    print('-------------process one-----------------')
    try:
        delayId = "\"" + eval(str(key, encoding="utf-8")) + "\""
        logging.info('get element is  {0}'.format(key))
        key = key.decode()
        jsonStr = f.getByKey(redisClient, key.replace('"', ''))
        print(jsonStr)
        jsonObject = json.loads(jsonStr.decode('utf-8'))
        text_path_list = []
        id_list = []
        label_list = jsonObject['labels']
        for fileObject in jsonObject['files']:
            text_path_list.append(fileObject['url'])
            id_list.append(fileObject['id'])
        print(text_path_list)
        print(id_list)
        print(label_list)
        classifications = text_classification._classification(text_path_list, id_list, label_list)  # --------------
        result = {"task": key, "classifications": classifications}  # --------------
        f.pushToQueue(redisClient, config.textClassificationFinishQueue, json.dumps(result))
        redisClient.zrem(config.textClassificationStartQueue, key)
    except Exception as e:
        print(e)


def delaySchduled(inc, redisClient):
    """Delay task method.
         Args:
            inc: scheduled task time.
            redisClient: redis client.
    """
    try:
        print("delay:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S"))
        redisClient.eval(delay_script.delayTaskLua, 1, config.textClassificationStartQueue, delayId, int(time.time()))
        schedule.enter(inc, 0, delaySchduled, (inc, redisClient))
    except Exception as e:
        print("delay error" + e)


def delayKeyThread(redisClient):
    """Delay task thread.
        Args:
             redisClient: redis client.
    """
    schedule.enter(0, 0, delaySchduled, (5, redisClient))
    schedule.run()
