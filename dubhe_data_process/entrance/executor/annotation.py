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
import time
import sys
sys.path.append("../../")
from entrance.executor import predict_with_print_box as yolo_demo
from common.log_config import setup_log


label_log = setup_log('dev', 'label.log')


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
    annotations = yolo_obj.yolo_inference(type_, id_list, annotation_url_list, image_path_list, label_list, coco_flag)
    return annotations[0:image_num] 
