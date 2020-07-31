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

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import web
import os
import string
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
import of_cnn_resnet
import numpy as np
from log_config import setup_log
from upload_config import Upload_cfg, MyApplication
urls = ('/auto_annotate', 'Upload')
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())


label_url = "api/data/datasets/files/annotations/auto/"
parser = argparse.ArgumentParser(description="config for imagenet label server")
parser.add_argument("-p", "--port", type=int, required=True)
parser.add_argument("-m", "--mode", type=str, default="test", required=False)
args = parser.parse_args()
url_json = './config/url.json'
with open(url_json) as f:
    url_dict = json.loads(f.read())
label_url = url_dict[args.mode] + label_url
port = args.port
taskQueue = Queue()
taskInImages = {}
base_path = "/nfs/"


des_folder = os.path.join('./log', args.mode)
if not os.path.exists(des_folder):
    os.makedirs(des_folder)

logging = setup_log(args.mode, 'imagenet-' + args.mode + '.log')


#############################label_server#####################################
def get_code():
    return ''.join(random.sample(string.ascii_letters + string.digits, 8))


def get_32code():
    return ''.join(random.sample(string.ascii_letters + string.digits, 32))


class Upload(Upload_cfg):
    """Recieve and analyze the post request"""

    def POST(self):
        try:
            super().POST()
            x = web.data()
            x = json.loads(x.decode())
            type_ = x['annotateType']
            if_imagenet = x['labelType']
            task_id = get_code()
            task_images = {}
            task_images[task_id] = {
                "input": {
                    'type': type_, 'data': x}, "output": {
                    "annotations": []}, 'if_imagenet': if_imagenet}
            logging.info(task_id)
            web.t_queue.put(task_images)
            return {"code": 200, "msg": "", "data": task_id}
        except Exception as e:
            logging.error("Error post")
            logging.error(e)
            return 'post error'


def imagenetProcess():
    """The implementation of imageNet auto labeling thread"""
    global taskQueue
    global label_url
    logging.info('ImageNet auto labeling server start'.center(66,'-'))
    logging.info(label_url)
    while True:
        try:
            task_dict = taskQueue.get()
            for task_id in task_dict:
                id_list = []
                image_path_list = []
                type_ = task_dict[task_id]["input"]['type']
                if_imagenet = task_dict[task_id]['if_imagenet']
                for file in task_dict[task_id]["input"]['data']["files"]:
                    id_list.append(file["id"])
                    image_path_list.append(base_path + file["url"])
                label_list = task_dict[task_id]["input"]['data']["labels"]
                image_num = len(image_path_list)
                logging.info(image_num)
                logging.info(image_path_list)
                annotations = []
                if if_imagenet == 2:
                    for inds in range(len(image_path_list)):
                        temp = {}
                        temp['id'] = id_list[inds]
                        score, ca_id = of_cnn_resnet.resnet_inf(
                            image_path_list[inds])
                        temp['annotation'] = [
                            {'category_id': int(ca_id), 'score': np.float(score)}]
                        temp['annotation'] = json.dumps(temp['annotation'])
                        annotations.append(temp)
                result = {"annotations": annotations}
                logging.info(result)
                send_data = json.dumps(result).encode()
                task_url = label_url + task_id
                headers = {'Content-Type': 'application/json'}
                req = urllib.request.Request(task_url, headers=headers)
                response = urllib.request.urlopen(
                    req, data=send_data, timeout=5)
                logging.info(task_url)
                logging.info(response.read())
                logging.info("End imagenet")

        except Exception as e:
            logging.error("Error imagenet_Process")
            logging.error(e)
            logging.info(label_url)
        time.sleep(0.01)


def imagenet_thread(no, interval):
    """Running the imageNet auto labeling thread"""
    imagenetProcess()


if __name__ == "__main__":
    of_cnn_resnet.init_resnet()
    _thread.start_new_thread(imagenet_thread, (5, 5))
    app = MyApplication(urls, globals())
    web.t_queue = taskQueue
    web.taskInImages = taskInImages
    app.run(port=port)
