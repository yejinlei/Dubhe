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
# !/usr/bin/env python3
# -*- coding: utf-8 -*-
from datetime import datetime
import sched
import os
import cv2
import numpy as np
import logging
import time
import json
import argparse
import sys
import codecs
import shutil

import luascript.delaytaskscript as delay_script
import common.config as config

from common.augment_utils.ACE import ACE_color
from common.augment_utils.dehaze import deHaze, addHaze
from common.augment_utils.hist_equalize import adaptive_hist_equalize
from common.log_config import setup_log

schedule = sched.scheduler(time.time, time.sleep)

delayId = ""
finish_key = {}
re_task_id = {}
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

# task url suffix
img_pro_url = 'api/data/datasets/'

# arguments
parser = argparse.ArgumentParser(description="config for image augmentation server")
parser.add_argument("-m", "--mode", type=str, default="test", required=False)
args = parser.parse_args()

# url concat(ip + port + suffix)
url_json = './common/config/url.json'
with open(url_json) as f:
    url_dict = json.loads(f.read())
img_pro_url = url_dict[args.mode] + img_pro_url

# creat task quene
base_path = "/nfs/"

# create log path and file
des_folder = os.path.join('./log', args.mode)
if not os.path.exists(des_folder):
    os.makedirs(des_folder)

logging = setup_log(args.mode, 'enhance-' + args.mode + '.log')
enhanceTaskId = ""


def start_enhance_task(enhanceTaskId, redisClient):
    """Enhance task method.
        Args:
            enhanceTaskId: enhance task id.
            redisClient: redis client.
    """
    global delayId
    detailKey = 'imgProcess:' + eval(str(enhanceTaskId[0], encoding="utf-8"))
    delayId = "\"" + eval(str(enhanceTaskId[0], encoding="utf-8")) + "\""
    print(detailKey)
    taskParameters = json.loads(redisClient.get(detailKey).decode())
    dataset_id = taskParameters['id']
    img_save_path = taskParameters['enhanceFilePath']
    ann_save_path = taskParameters["enhanceAnnotationPath"]
    file_list = taskParameters['fileDtos']
    nums_, img_path_list, ann_path_list = img_ann_list_gen(file_list)
    process_type = taskParameters['type']
    re_task_id = eval(str(enhanceTaskId[0], encoding="utf-8"))
    img_process_config = [dataset_id, img_save_path,
                          ann_save_path, img_path_list,
                          ann_path_list, process_type, re_task_id]
    image_enhance_process(img_process_config, redisClient)
    logging.info(str(nums_) + ' images for augment')


def img_ann_list_gen(file_list):
    """Analyze the json request and convert to list"""
    nums_ = len(file_list)
    img_list = []
    ann_list = []
    for i in range(nums_):
        img_list.append(file_list[i]['filePath'])
        ann_list.append(file_list[i]['annotationPath'])
    return nums_, img_list, ann_list


def image_enhance_process(img_task, redisClient):
    """The implementation of image augmentation thread"""
    global img_pro_url
    global finish_key
    global re_task_id
    logging.info('img_process server start'.center(66, '-'))
    logging.info(img_pro_url)
    try:
        dataset_id = img_task[0]
        img_save_path = img_task[1]
        ann_save_path = img_task[2]
        img_list = img_task[3]
        ann_list = img_task[4]
        method = img_task[5]
        re_task_id = img_task[6]
        suffix = '_enchanced_' + re_task_id
        logging.info("dataset_id " + str(dataset_id))

        finish_key = {"processKey": re_task_id}
        finish_data = {"id": re_task_id,
                       "suffix": suffix}
        for j in range(len(ann_list)):
            img_path = img_list[j]
            ann_path = ann_list[j]
            img_process(suffix, img_path, ann_path,
                        img_save_path, ann_save_path, method)

        redisClient.lpush(config.imgProcessFinishQueue, json.dumps(finish_key, separators=(',', ':')))
        redisClient.set("imgProcess:finished:" + re_task_id, json.dumps(finish_data))
        redisClient.zrem(config.imgProcessStartQueue, "\"" + re_task_id + "\"")
        logging.info('suffix:' + suffix)
        logging.info("End img_process of dataset:" + str(dataset_id))

    except Exception as e:
        redisClient.lpush(config.imgProcessFailedQueue, json.dumps(finish_key, separators=(',', ':')))
        redisClient.zrem(config.imgProcessStartQueue, "\"" + re_task_id + "\"")
        logging.info(img_pro_url)
        logging.error("Error imgProcess")
        logging.error(e)
    time.sleep(0.01)


def img_process(suffix, img_path, ann_path, img_save_path, ann_save_path, method_ind):
    """Process images and save in specified path"""
    inds2method = {1: deHaze, 2: addHaze, 3: ACE_color, 4: adaptive_hist_equalize}
    method = inds2method[method_ind]
    img_raw = cv2.imdecode(np.fromfile(img_path.encode('utf-8'), dtype=np.uint8), 1)
    img_suffix = os.path.splitext(img_path)[-1]
    ann_name = ann_path.replace(ann_save_path, '')
    if method_ind <= 3:
        processed_img = method(img_raw / 255.0) * 255
    else:
        processed_img = method(img_raw)
    cv2.imwrite(img_save_path + ann_name + suffix + img_suffix,
                processed_img.astype(np.uint8))
    shutil.copyfile(ann_path.encode('utf-8'), (ann_path + suffix).encode('utf-8'))


def delaySchduled(inc, redisClient):
    """Delay task method.
         Args:
            inc: scheduled task time.
            redisClient: redis client.
    """
    try:
        logging.info("delay:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S") + ":" + delayId)
        redisClient.eval(delay_script.delayTaskLua, 1, config.imgProcessStartQueue, delayId, int(time.time()))
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
