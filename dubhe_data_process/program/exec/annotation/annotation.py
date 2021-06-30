# !/usr/bin/env python
# -*- coding:utf-8 -*-

"""
Copyright 2020 Tianshu AI Platform. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
=============================================================
"""

import codecs
import os
import sched
import logging
import time
import sys

from program.exec.annotation import predict_with_print_box as yolo_demo
from common.config.log_config import setup_log
from abc import ABC
from program.abstract.algorithm import Algorithm

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)

schedule = sched.scheduler(time.time, time.sleep)
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

label_log = setup_log('dev', 'label.log')


class Annotation(Algorithm, ABC):

    def __init__(self):
        pass

    def execute(task):
        return Annotation.annotationExecutor(task)

    def annotationExecutor(jsonObject):
        """Annotation task method.
                    Args:
                      redisClient: redis client.
                      key: annotation task key.
        """
        print('-------------process one-----------------')
        try:
            image_path_list = []
            id_list = []
            annotation_url_list = []
            label_list = jsonObject['labels']
            for fileObject in jsonObject['files']:
                pic_url = '/nfs/' + fileObject['url']
                image_path_list.append(pic_url)
                annotation_url = pic_url.replace("origin/", "annotation/")
                annotation_url_list.append(os.path.splitext(annotation_url)[0])
                isExists = os.path.exists(os.path.dirname(annotation_url))
                if not isExists:
                    try:
                        os.makedirs(os.path.dirname(annotation_url))
                    except Exception as exception:
                        logging.error(exception)
                id_list.append(fileObject['id'])
            print(image_path_list)
            print(annotation_url_list)
            print(label_list)
            coco_flag = 0
            if "labelType" in jsonObject:
                label_type = jsonObject['labelType']
                if label_type == 3:
                    coco_flag = 80
            annotations = Annotation._annotation(0, image_path_list, id_list, annotation_url_list, label_list,
                                                 coco_flag);
            finish_data = {"reTaskId": jsonObject["reTaskId"], "annotations": annotations}
            return finish_data, True
        except Exception as e:
            print(e)
            finish_data = {"reTaskId": jsonObject["reTaskId"], "annotations": annotations}
            return finish_data, True

    @staticmethod
    def _init():
        print('init yolo_obj')
        global yolo_obj
        yolo_obj = yolo_demo.YoloInference(label_log)

    def _annotation(type_, image_path_list, id_list, annotation_url_list, label_list, coco_flag=0):
        """Perform automatic annotation task."""
        image_num = len(image_path_list)
        if image_num < 16:
            for i in range(16 - image_num):
                image_path_list.append(image_path_list[0])
                id_list.append(id_list[0])
                annotation_url_list.append(annotation_url_list[0])
        image_num = len(image_path_list)
        annotations = yolo_obj.yolo_inference(type_, id_list, annotation_url_list, image_path_list, label_list,
                                              coco_flag)
        return annotations[0:image_num]
