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
import web
import os
import string
import cv2
import numpy as np
import _thread
import logging
import urllib
from queue import Queue
import time
import random
import json
import argparse
import sys
import codecs
import shutil
from augment_utils.ACE import ACE_color
from augment_utils.dehaze import deHaze, addHaze
from augment_utils.hist_equalize import adaptive_hist_equalize
from log_config import setup_log
from upload_config import Upload_cfg, MyApplication

urls = ('/img_process', 'Image_augmentation')
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

# task url suffix
img_pro_url = 'api/data/datasets/'

# arguments
parser = argparse.ArgumentParser(description="config for image augmentation server")
parser.add_argument("-p", "--port", type=int, required=True)
parser.add_argument("-m", "--mode", type=str, default="test", required=False)
args = parser.parse_args()

# url concat(ip + port + suffix)
url_json = './config/url.json'
with open(url_json) as f:
    url_dict = json.loads(f.read())
img_pro_url = url_dict[args.mode] + img_pro_url
port = args.port

# creat task quene
imageProcessQuene = Queue()
base_path = "/nfs/"

# create log path and file
des_folder = os.path.join('./log', args.mode)
if not os.path.exists(des_folder):
    os.makedirs(des_folder)

logging = setup_log(args.mode, 'enhance-' + args.mode + '.log')


class Image_augmentation(Upload_cfg):
    """Recieve and analyze the post request"""

    def POST(self):
        try:
            super().POST()
            x = web.data()
            x = json.loads(x.decode())
            dataset_id = x['id']
            img_save_path = x['enhanceFilePath']
            ann_save_path = x["enhanceAnnotationPath"]
            file_list = x['fileDtos']
            nums_, img_path_list, ann_path_list = img_ann_list_gen(file_list)
            process_type = x['type']
            re_task_id = ''.join(random.sample(string.ascii_letters + string.digits, 8))
            img_process_config = [dataset_id, img_save_path,
                                  ann_save_path, img_path_list,
                                  ann_path_list, process_type, re_task_id]
            web.t_queue2.put(img_process_config)
            logging.info(str(nums_) + ' images for augment')
            return {"code": 200, "msg": "", "data": re_task_id}
        except Exception as e:
            print(e)
            print("Error Post")
            logging.error("Error post")
            logging.error(e)
            return 'post error'


def image_process_thread():
    """The implementation of image augmentation thread"""
    global img_pro_url
    global imageProcessQuene

    logging.info('img_process server start'.center(66, '-'))
    logging.info(img_pro_url)
    task_cond = []
    while True:
        try:
            img_task = imageProcessQuene.get()
            if img_task and img_task[0] not in task_cond:
                index = len(task_cond)
                task_cond.append(img_task[0])
                dataset_id = img_task[0]
                img_save_path = img_task[1]
                ann_save_path = img_task[2]
                img_list = img_task[3]
                ann_list = img_task[4]
                method = img_task[5]
                re_task_id = img_task[6]
                suffix = '_enchanced_' + re_task_id
                logging.info("dataset_id " + str(dataset_id))
                for j in range(len(ann_list)):
                    img_path = img_list[j]
                    ann_path = ann_list[j]
                    img_process(suffix, img_path, ann_path,
                                img_save_path, ann_save_path, method)

                task_url = img_pro_url + 'enhance/finish'
                send_data = {"id": re_task_id,
                             "suffix": suffix}

                headers = {'Content-Type': 'application/json'}
                req = urllib.request.Request(task_url,
                                             data=json.dumps(send_data).encode(),
                                             headers=headers)
                response = urllib.request.urlopen(req, timeout=5)
                logging.info('suffix:' + suffix)
                logging.info(task_url)
                logging.info(response.read())
                logging.info("End img_process of dataset:" + str(dataset_id))
                task_cond.pop(index)
            else:
                continue
        except Exception as e:
            logging.info(img_pro_url)
            logging.error("Error imgProcess")
            logging.error(e)
        time.sleep(0.01)


def img_ann_list_gen(file_list):
    """Analyze the json request and convert to list"""
    nums_ = len(file_list)
    img_list = []
    ann_list = []
    for i in range(nums_):
        img_list.append(file_list[i]['filePath'])
        ann_list.append(file_list[i]['annotationPath'])
    return nums_, img_list, ann_list


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


def img_process_thread(no, interval):
    """Running the image augmentation thread"""
    image_process_thread()


if __name__ == "__main__":
    _thread.start_new_thread(img_process_thread, (5, 5))
    app = MyApplication(urls, globals())
    web.t_queue2 = imageProcessQuene
    app.run(port=port)