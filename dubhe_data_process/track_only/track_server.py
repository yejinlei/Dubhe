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

#track_only
自动标定工具跟踪算法
开源引用
1.本代码中的卡尔曼滤波及匈牙利算法代码来此开源项目：https://github.com/ZQPei/deep_sort_pytorch
2.本代码中使用的开源python包有：opencv、numpy、scipy、web.py。

"""
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import _thread
import argparse
import json
import logging
import os
import random
import string
import time
import urllib
from queue import Queue

import web
from hog_track import *

urls = ('/xxxxx', 'Upload')

taskQueue = Queue()
taskInImages = {}
base_path = "/xxx"

#配置参数，根据自己的端口号，配置相应的参数
parser = argparse.ArgumentParser(description='manual to this script')
parser.add_argument("--path", type=str, default="")
parser.add_argument("--callback_urls", type=str, default="")
parser.add_argument("--port", type=int, default=0000)
parser.add_argument("--log_name", type=str, default="")
args = parser.parse_args()
base_path = args.path
callback_urls = args.callback_urls

#日志打印模块，用于保存服务的状态日志
logging.basicConfig(level=logging.DEBUG,  # 控制台打印的日志级别
                    filename='tracklog_' + args.log_name + '.txt',
                    filemode='a',
                    format='%(asctime)s - %(message)s'
                    )
logging.info('------------------------------------track server start-----------------------------')
logging.info(args.path)
logging.info(args.callback_urls)
logging.info(args.port)


def get_code():
    return ''.join(random.sample(string.ascii_letters + string.digits, 8))


def get_32code():
    return ''.join(random.sample(string.ascii_letters + string.digits, 32))


#POST web请求服务，用于接收其他端口的自动标定跟踪请求
class Upload:
    def GET(self):
        x = web.input()
        print(x)
        web.header("Access-Control-Allow-Origin", "*")
        web.header("Access-Control-Allow-Credentials", "true")
        web.header(
            'Access-Control-Allow-Headers',
            'Content-Type, Access-Control-Allow-Origin, Access-Control-Allow-Headers, X-Requested-By, Access-Control-Allow-Methods')
        web.header('Access-Control-Allow-Methods', 'POST, GET, PUT, DELETE')
        return """<html><head></head><body>please send data in post
</body></html>"""

    #post请求，用于接收自动标定跟踪请求，并将请求任务防止缓存队列，逐次处理
    def POST(self):
        try:
            web.header("Access-Control-Allow-Origin", "*")
            web.header("Access-Control-Allow-Credentials", "true")
            web.header(
                'Access-Control-Allow-Headers',
                'Content-Type, Access-Control-Allow-Origin, Access-Control-Allow-Headers, X-Requested-By, Access-Control-Allow-Methods')
            web.header(
                'Access-Control-Allow-Methods',
                'POST, GET, PUT, DELETE')
            x = web.data()
            x = json.loads(x.decode())
            taskId = get_code()
            taskInImages = {}
            taskInImages[taskId] = {'data': x}
            print("track Random_code:", taskId)
            logging.info(taskId)
            video_id = x['id']
            images_data = x['images']
            image_num = len(images_data)
            logging.info(video_id)
            logging.info(image_num)
            web.t_queue.put(taskInImages)
            return {"code": 200, "msg": "", "data": taskId}
        except Exception as e:
            logging.error("Error post")
            logging.error(e)
            print(e)
            print("Error Post")
            return 'post error'

#跟踪处理进程函数，不停地向缓存队列里面获取任务，并进行跟踪处理
def trackProcess():
    global taskQueue
    global callback_urls
    global callback_urls_addr
    while True:
        try:
            #获取队列中的任务
            task_dict = taskQueue.get()
            for taskId in task_dict:
                task_data = task_dict[taskId]['data']
                video_id = task_data['id']
                image_list = []
                label_list = []
                images_data = task_data['images']
                
                #获取任务的所有图像和标签，并验证其是否存在
                for file in images_data:
                    filePath = base_path + file['filePath']
                    annotationPath = base_path + file['annotationPath']
                    if not os.path.exists(filePath):
                        continue
                    if not os.path.exists(annotationPath):
                        continue
                    image_list.append(filePath)
                    label_list.append(annotationPath)
                image_num = len(label_list)
                logging.info(image_num)

                if len(image_list) != len(label_list):
                    logging.error("Error image_list len != label_list len")
                    print("Error image_list len != label_list len")
                track_det = Detector(
                    'xxx.avi',
                    min_confidence=0.35,
                    max_cosine_distance=0.2,
                    max_iou_distance=0.7,
                    max_age=30,
                    out_dir='results/')
                track_det.write_img = False
                #跟踪处理
                RET = track_det.run_track(image_list, label_list)
                logging.info(RET)
                if RET == 'OK':
                    result = {
                        "code": 200,
                        "msg": 'success',
                        "data": 'null',
                        "traceId": 'null'}
                else:
                    result = {
                        "code": 199,
                        "msg": RET,
                        "data": 'null',
                        "traceId": 'null'}
                send_data = json.dumps(result).encode()
                callback_urls_addr = 'http://' + callback_urls + \
                                     '/xxx/' + str(video_id)
                logging.info(callback_urls_addr)
                headers = {'Content-Type': 'application/json'}
                #处理完毕后向相应端口发送处理完毕请求。
                req = urllib.request.Request(
                    callback_urls_addr, headers=headers)
                response = urllib.request.urlopen(
                    req, data=send_data, timeout=5)
                logging.info(response.read())
                logging.info("End track")
                print("End track")
        except Exception as e:
            logging.error("Error trackProcess")
            logging.error(e)
            print("Error trackProcess")
            print(e)
        time.sleep(0.01)

#开启跟踪进程
def track_thread(no, interval):
    print('track_thread on')
    trackProcess()

#采用web.py的类
class MyApplication(web.application):
    def run(self, port=0000, *middleware):
        func = self.wsgifunc(*middleware)
        return web.httpserver.runsimple(func, ('0.0.0.0', port))


if __name__ == "__main__":
    #开启跟踪线程
    _thread.start_new_thread(track_thread, (5, 5))
    #开启自己的应用服务
    app = MyApplication(urls, globals())

    #任务获取队列
    web.t_queue = taskQueue
    web.taskInImages = taskInImages
    app.run(port=args.port)
