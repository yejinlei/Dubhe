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
import json
import os
from abc import ABC
from program.abstract.algorithm import Algorithm

import cv2

datasetIdKey = ""


class Videosample(Algorithm, ABC):

    def __init__(self):
        pass

    def execute(task):
        return Videosample.sampleProcess(task)

    def sampleProcess(taskParameters):
        """Video sampling method.
            Args:
              taskParameters: taskParameters.
        """
        global datasetIdKey
        path = taskParameters['path']
        frameList = taskParameters['frames']
        datasetId = taskParameters['datasetId']
        task_id = taskParameters['id']
        datasetIdJson = {'datasetIdKey': datasetId}
        datasetIdKey = json.dumps(datasetIdJson, separators=(',', ':'))
        result = True
        try:
            videoName = path.split('/')[-1]
            save_path = path.split(videoName)[0].replace("video", "origin")
            is_exists = os.path.exists(save_path)
            if not is_exists:
                os.makedirs(save_path)
                print('path of %s is build' % save_path)
            else:
                print('path of %s already exist and start' % save_path)
            cap = cv2.VideoCapture(path)
            pic_name_list = []
            finish_json = {}
            for i in frameList:
                cap.set(cv2.CAP_PROP_POS_FRAMES, i)
                success, video_capture = cap.read()
                # 保存图片
                if success is True and video_capture is not None:
                    save_name = save_path + videoName.split('.')[0] + '_' + str(i) + '.jpg'
                    cv2.imwrite(save_name, video_capture)
                    pic_name_list.append(save_name)
                    print('image of %s is saved' % save_name)
            pic_name_list.reverse()
            finish_json['pictureNames'] = pic_name_list
            finish_json['datasetIdAndSub'] = datasetId
            finish_json['id'] = task_id
            print('video is all read')
            return finish_json, result
        except Exception as e:
            print(e)
            failed_json = {'datasetIdAndSub': datasetId}
            result = False
            return failed_json, result
