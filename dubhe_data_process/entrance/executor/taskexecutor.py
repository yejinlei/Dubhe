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
import os
import sched
import sys
import json
import logging
import time
import common.RedisUtil as f
import common.config as config
from entrance.executor import annotation as annotation
from datetime import datetime
import luascript.delaytaskscript as delay_script

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)

schedule = sched.scheduler(time.time, time.sleep)
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

delayId = ""


def annotationExecutor(redisClient, key):
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
        jsonStr = f.getByKey(redisClient, key.replace('"', ''));
        print(jsonStr)
        jsonObject = json.loads(jsonStr.decode('utf-8'));
        image_path_list = []
        id_list = []
        annotation_url_list = []
        label_list = []
        label_list = jsonObject['labels']
        for fileObject in jsonObject['files']:
            pic_url = '/nfs/' + fileObject['url']
            image_path_list.append(pic_url)
            annotation_url = pic_url.replace("origin/", "annotation/")
            annotation_url_list.append(os.path.splitext(annotation_url)[0])
            isExists = os.path.exists(os.path.dirname(annotation_url))
            if not isExists:
                os.makedirs(os.path.dirname(annotation_url))
            id_list.append(fileObject['id'])
        print(image_path_list)
        print(annotation_url_list)
        print(label_list)
        coco_flag = 0
        if "labelType" in jsonObject:
            label_type = jsonObject['labelType']
            if label_type == 3:
                coco_flag = 80
        annotations = annotation._annotation(0, image_path_list, id_list, annotation_url_list, label_list, coco_flag);
        result = {"task": key, "annotations": annotations}
        f.pushToQueue(redisClient, config.annotationFinishQueue, json.dumps(result))
        redisClient.zrem(config.annotationStartQueue, key)
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
        redisClient.eval(delay_script.delayTaskLua, 1, config.annotationStartQueue, delayId, int(time.time()))
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
