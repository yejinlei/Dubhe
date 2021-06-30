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
import sched
import sys
import logging
import time
from program.exec.text_classification import classify_by_textcnn as classify
from abc import ABC
from program.abstract.algorithm import Algorithm

logging.basicConfig(format='%(asctime)s - %(pathname)s[line:%(lineno)d] - %(levelname)s: %(message)s',
                    level=logging.DEBUG)

schedule = sched.scheduler(time.time, time.sleep)
sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

delayId = ""


class Text_classification(Algorithm, ABC):

    def __init__(self):
        pass

    def execute(task):
        return Text_classification.textClassificationExecutor(task)

    def textClassificationExecutor(jsonObject):
        """Annotation task method.
                    Args:
                      redisClient: redis client.
                      key: annotation task key.
        """
        global delayId
        result = True
        print('-------------process one-----------------')
        try:
            text_path_list = []
            id_list = []
            label_list = jsonObject['labels']
            for fileObject in jsonObject['files']:
                text_path_list.append(fileObject['url'])
                id_list.append(fileObject['id'])
            print(text_path_list)
            print(id_list)
            print(label_list)
            classifications = Text_classification._classification(text_path_list, id_list, label_list)  # --------------
            finished_json = {"reTaskId": jsonObject['reTaskId'], "classifications": classifications}
            return finished_json, result
        except Exception as e:
            print(e)

    @staticmethod
    def _init():
        print('init classify_obj')
        global classify_obj
        classify_obj = classify.TextCNNClassifier()  # label_log

    def _classification(text_path_list, id_list, label_list):
        """Perform automatic text classification task."""
        textnum = len(text_path_list)
        batched_num = ((textnum - 1) // classify.BATCH_SIZE + 1) * classify.BATCH_SIZE
        for i in range(batched_num - textnum):
            text_path_list.append(text_path_list[0])
            id_list.append(id_list[0])
        annotations = classify_obj.inference(text_path_list, id_list, label_list)  #
        return annotations[0:textnum]
