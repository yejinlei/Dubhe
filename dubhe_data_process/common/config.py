"""
/**
* Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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
# coding:utf-8

import json

host = ''
port = 6379
db = 0
password = ''

# text_classification
textClassificationQueue = 'text_classification_task_queue'
textClassificationStartQueue = 'text_classification_processing_queue'
textClassificationFinishQueue = 'text_classification_finished_queue'

# annotation
queue = 'annotation_task_queue'
annotationStartQueue = 'annotation_processing_queue'
annotationFinishQueue = 'annotation_finished_queue'

# imagenet
imagenetTaskQueue = 'imagenet_task_queue'
imagenetStartQueue = 'imagenet_processing_queue'
imagenetFinishQueue = 'imagenet_finished_queue'

# ofrecord
ofrecordTaskQueue = 'ofrecord_task_queue'
ofrecordStartQueue = 'ofrecord_processing_queue'
ofrecordFinishQueue = 'ofrecord_finished_queue'

# track
trackTaskQueue = 'track_task_queue'
trackStartQueue = 'track_processing_queue'
trackFinishQueue = 'track_finished_queue'
trackFailedQueue = 'track_failed_queue'

# videosample
videoPendingQueue = "videoSample_unprocessed"
videoStartQueue = "videoSample_processing"
videoFinishQueue = "videoSample_finished"
videoFailedQueue = "videoSample_failed"

# lungsegmentation
dcmTaskQueue = "dcm_task_queue"
dcmStartQueue = "dcm_processing_queue"
dcmFinishQueue = "dcm_finished_queue"

# imgprocess
imgProcessTaskQueue = 'imgProcess_unprocessed'
imgProcessFinishQueue = 'imgProcess_finished'
imgProcessStartQueue = "imgProcess_processing"
imgProcessFailedQueue = "imgProcess_failed"

threadCount = 5

configPath = "/root/algorithm/config.json"
sign = "/root/algorithm/sign"


def loadJsonData(path):
    with open(path, 'r', encoding='utf8') as fp:
        jsonData = json.load(fp)
    return jsonData
