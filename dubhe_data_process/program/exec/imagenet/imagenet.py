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
import os
import sched

import logging
import time
import json
import common.util.algorithm.of_cnn_resnet as of_cnn_resnet
import numpy as np
from abc import ABC
from program.abstract.algorithm import Algorithm

schedule = sched.scheduler(time.time, time.sleep)

base_path = "/nfs/"
delayId = ""


class Imagenet(Algorithm, ABC):

    @staticmethod
    def _init():
        of_cnn_resnet.init_resnet()
        logging.info('env init finished')

    def __init__(self):
        pass

    def execute(task):
        return Imagenet.process(task)

    def process(task_dict):
        """Imagenet task method.
            Args:
                task_dict: imagenet task details.
                key: imagenet task key.
        """
        id_list = []
        image_path_list = []
        annotation_path_list = []
        for file in task_dict["files"]:
            id_list.append(file["id"])
            image_path = base_path + file["url"]
            image_path_list.append(image_path)
            annotation_url = image_path.replace("origin/", "annotation/")
            annotation_path_list.append(os.path.splitext(annotation_url)[0])
            isExists = os.path.exists(os.path.dirname(annotation_url))
            if not isExists:
                try:
                    os.makedirs(os.path.dirname(annotation_url))
                except Exception as exception:
                    logging.error(exception)
        label_list = task_dict["labels"]
        image_num = len(image_path_list)
        annotations = []
        for inds in range(len(image_path_list)):
            temp = {}
            temp['id'] = id_list[inds]
            score, ca_id = of_cnn_resnet.resnet_inf(image_path_list[inds])
            temp['annotation'] = [{'category_id': int(ca_id), 'score': np.float(score)}]
            temp['annotation'] = json.dumps(temp['annotation'])
            annotations.append(temp)
            with open(annotation_path_list[inds], 'w') as w:
                w.write(temp['annotation'])
        finish_data = {"annotations": annotations, "reTaskId": task_dict["reTaskId"]}
        return finish_data, True
