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

import logging
import time
import cv2
import numpy as np
import shutil
import os
from abc import ABC
from program.abstract.algorithm import Algorithm
from common.util.algorithm.ACE import ACE_color
from common.util.algorithm.dehaze import deHaze, addHaze
from common.util.algorithm.hist_equalize import adaptive_hist_equalize


class Imgprocess(Algorithm, ABC):

    def __init__(self):
        pass

    def execute(task):
        return Imgprocess.start_enhance_task(task)

    def start_enhance_task(taskParameters):
        """
        Enhance task method.
        Args:
            enhanceTaskId: enhance task id.
            redisClient: redis client.
        """
        dataset_id = taskParameters['id']
        img_save_path = taskParameters['enhanceFilePath']
        ann_save_path = taskParameters["enhanceAnnotationPath"]
        file_list = taskParameters['fileDtos']
        nums_, img_path_list, ann_path_list = Imgprocess.img_ann_list_gen(file_list)
        process_type = taskParameters['type']
        re_task_id = taskParameters['reTaskId']
        img_process_config = [dataset_id, img_save_path,
                              ann_save_path, img_path_list,
                              ann_path_list, process_type, re_task_id]
        return Imgprocess.image_enhance_process(img_process_config)
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

    def image_enhance_process(img_task):
        """The implementation of image augmentation thread"""
        global finish_key
        global re_task_id
        logging.info('img_process server start'.center(66, '-'))
        result = True
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
                Imgprocess.img_process(suffix, img_path, ann_path,
                            img_save_path, ann_save_path, method)

            logging.info('suffix:' + suffix)
            logging.info("End img_process of dataset:" + str(dataset_id))
            return finish_data, result

        except Exception as e:
            result = False
            return finish_data, result
            logging.error("Error imgProcess")
            logging.error(e)
        time.sleep(0.01)

    def img_process(suffix, img_path, ann_path, img_save_path, ann_save_path, method_ind):
        """Process images and save in specified path"""
        inds2method = {1: deHaze, 2: addHaze, 3: ACE_color, 4: adaptive_hist_equalize}
        method = inds2method[method_ind]
        img_raw = cv2.imdecode(np.fromfile(img_path.encode('utf-8'), dtype=np.uint8), 1)
        img_suffix = os.path.splitext(img_path)[-1]
        ann_name = os.path.basename(ann_path)
        if method_ind <= 3:
            processed_img = method(img_raw / 255.0) * 255
        else:
            processed_img = method(img_raw)
        cv2.imwrite(img_save_path + "/" + ann_name + suffix + img_suffix,
                    processed_img.astype(np.uint8))
        shutil.copyfile(ann_path.encode('utf-8'), (ann_save_path + "/" + ann_name + suffix).encode('utf-8'))