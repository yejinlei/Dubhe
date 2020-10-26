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
import _thread
import argparse
import codecs
import json
import os
import random
import string
import sys
import time
import urllib
from queue import Queue

import predict_with_print_box as yolo_demo
import web
from upload_config import Upload_cfg, MyApplication
from log_config import setup_log

'''Config urls and chinese coding'''
urls = ('/auto_annotate', 'Upload')
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

'''Set port and mode'''
parser = argparse.ArgumentParser(description="config for label server")
parser.add_argument("-p", "--port", type=int, required=True)
parser.add_argument("-m", "--mode", type=str, default="test", required=False)
args = parser.parse_args()

'''Set path'''
base_path = "/nfs/"
label_url = "api/data/datasets/files/annotations/auto/"
url_json = './config/url.json'

'''Init task queue and log'''
with open(url_json) as f:
    url_dict = json.loads(f.read())
label_url = url_dict[args.mode] + label_url
port = args.port
taskQueue = Queue()
taskInImages = {}
des_folder = os.path.join('./log', args.mode)
if not os.path.exists(des_folder):
    os.makedirs(des_folder)
label_log = setup_log(args.mode, 'label-' + args.mode + '.log')


def get_code():
    """Generate task_id"""
    return ''.join(random.sample(string.ascii_letters + string.digits, 8))


class Upload(Upload_cfg):
    """Recieve and analyze the post request"""

    def POST(self):
        try:
            super().POST()
            x = web.data()
            x = json.loads(x.decode())
            type_ = x['annotateType']
            task_id = get_code()
            task_images = {}
            task_images[task_id] = {"input": {'type': type_, 'data': x}, "output": {"annotations": []}}
            print("Random_code:", task_id)
            label_log.info(task_id)
            label_log.info('web.t_queue length:%s' % web.t_queue.qsize())
            label_log.info('Recv task_images:%s' % task_images)
            web.t_queue.put(task_images)
            return {"code": 200, "msg": "", "data": task_id}
        except Exception as e:
            label_log.error("Error post")
            label_log.error(e)
            return 'post error'


def bgProcess():
    """The implementation of automatic_label generating thread"""
    global taskQueue
    global label_url
    label_log.info('auto label server start'.center(66, '-'))
    label_log.info(label_url)
    while True:
        try:
            task_dict = taskQueue.get()
            for task_id in task_dict:
                id_list = []
                image_path_list = []
                type_ = task_dict[task_id]["input"]['type']
                for file in task_dict[task_id]["input"]['data']["files"]:
                    id_list.append(file["id"])
                    image_path_list.append(base_path + file["url"])
                label_list = task_dict[task_id]["input"]['data']["labels"]
                coco_flag = 0
                if "labelType" in task_dict[task_id]["input"]['data']:
                    label_type = task_dict[task_id]["input"]['data']["labelType"]
                    if label_type == 3:
                        coco_flag = 80
                label_log.info(coco_flag)
                image_num = len(image_path_list)
                if image_num < 16:
                    for i in range(16 - image_num):
                        image_path_list.append(image_path_list[0])
                        id_list.append(id_list[0])
                label_log.info(image_num)
                label_log.info(image_path_list)
                annotations = yolo_obj.yolo_inference(type_, id_list, image_path_list, label_list, coco_flag)
                annotations = annotations[0:image_num]
                result = {"annotations": annotations}
                label_log.info('Inference complete %s' % task_id)
                send_data = json.dumps(result).encode()
                task_url = label_url + task_id
                headers = {'Content-Type': 'application/json'}
                req = urllib.request.Request(task_url, headers=headers)
                response = urllib.request.urlopen(req, data=send_data, timeout=2)
                label_log.info(task_url)
                label_log.info(response.read())
                label_log.info("End automatic label")

        except Exception as e:
            label_log.error("Error bgProcess")
            label_log.error(e)
            label_log.info(label_url)
        time.sleep(0.01)


def bg_thread(no, interval):
    """Running the automatic_label generating thread"""
    bgProcess()


if __name__ == "__main__":
    yolo_obj = yolo_demo.YoloInference(label_log)
    _thread.start_new_thread(bg_thread, (5, 5))
    app = MyApplication(urls, globals())
    web.t_queue = taskQueue
    web.taskInImages = taskInImages
    app.run(port=port)
