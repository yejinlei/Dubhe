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
# -*- coding: utf-8 -*-

import logging
import json
import os
import struct
import cv2
import sched
import numpy as np
import oneflow.core.record.record_pb2 as of_record
import luascript.delaytaskscript as delay_script
import time
import common.config as config
from datetime import datetime

schedule = sched.scheduler(time.time, time.sleep)

delayId = ""

class ImageCoder(object):
    """Helper class that provides image coding utilities."""

    def __init__(self, size=None):
        self.size = size

    def _resize(self, image_data):
        if self.size is not None and image_data.shape[:2] != self.size:
            return cv2.resize(image_data, self.size)
        return image_data

    def image_to_jpeg(self, image_data):
        image_data = cv2.imdecode(np.frombuffer(image_data, np.uint8), 1)
        image_data = self._resize(image_data)
        return cv2.imencode(".jpg", image_data)[1].tobytes(
        ), image_data.shape[0], image_data.shape[1]


def _process_image(filename, coder):
    """Process a single image file.
        Args:
            filename: string, path to an image file e.g., '/path/to/example.JPG'.
            coder: instance of ImageCoder to provide image coding utils.
        Returns:
            image_buffer: string, JPEG encoding of RGB image.
            height: integer, image height in pixels.
            width: integer, image width in pixels.
    """
    # Read the image file.
    with open(filename, 'rb') as f:
        image_data = f.read()
        image_data, height, width = coder.image_to_jpeg(image_data)

        return image_data, height, width


def _bytes_feature(value):
    """Wrapper for inserting bytes features into Example proto."""
    return of_record.Feature(bytes_list=of_record.BytesList(value=[value]))


def dense_to_one_hot(labels_dense, num_classes):
    """Convert class labels from scalars to one-hot vectors."""
    num_labels = labels_dense.shape[0]
    index_offset = np.arange(num_labels) * num_classes
    labels_one_hot = np.zeros((num_labels, num_classes))
    labels_one_hot.flat[index_offset + labels_dense.ravel()] = 1
    return labels_one_hot


def extract_img_label(names, path):
    """Extract the images and labels into np array [index].
    Args:
      f: A file object that contain images and annotations.
    Returns:
      data: A 4D uint8 np array [index, h, w, depth].
      labels: a 1D uint8 np array.
      num_img: the number of images.
    """
    train_img = os.path.join(path, 'origin/')
    train_label = os.path.join(path, 'annotation/')
    num_imgs = len(names)
    data = []
    labels = []
    print('^^^^^^^^^^ start img_set for sycle')
    for i in names:
        name = os.path.splitext(i)[0]
        print(name)
        coder = ImageCoder((224, 224))
        image_buffer, height, width = _process_image(
            os.path.join(train_img, i), coder)

        data += [image_buffer]

        if os.path.exists(os.path.join(train_label, name)):

            with open(os.path.join(train_label, name), "r", encoding='utf-8') as jsonFile:
                la = json.load(jsonFile)
                if la:
                    labels += [la[0]['category_id']]
                else:
                    data.pop()
                    num_imgs -= 1
        else:
            print('File is not found')
    print('^^^^^^^^^ img_set for end')
    data = np.array(data)
    labels = np.array(labels)
    print(data.shape, labels.shape)
    return num_imgs, data, labels


def execute(src_path, desc, label_map, files, part_id, key):
    """Execute ofrecord task method."""
    global delayId
    delayId = delayId = "\"" + eval(str(key, encoding="utf-8")) + "\""
    logging.info(part_id)
    num_imgs, images, labels = extract_img_label(files, src_path)
    keys = sorted(list(map(int, label_map.keys())))
    for i in range(len(keys)):
        label_map[str(keys[i])] = i
    if not num_imgs:
        return False, 0, 0
    try:
        os.makedirs(desc)
    except Exception as e:
        print('{} exists.'.format(desc))
    filename = 'part-{}'.format(part_id)
    filename = os.path.join(desc, filename)
    f = open(filename, 'wb')
    print(filename)
    for i in range(num_imgs):
        img = images[i]
        label = label_map[str(labels[i])]
        sample = of_record.OFRecord(feature={
            'class/label': of_record.Feature(int32_list=of_record.Int32List(value=[label])),
            'encoded': _bytes_feature(img)
        })
        size = sample.ByteSize()
        f.write(struct.pack("q", size))
        f.write(sample.SerializeToString())
    if f:
        f.close()

def delaySchduled(inc, redisClient):
    """Delay task method.
        Args:
            inc: scheduled task time.
            redisClient: redis client.
    """
    try:
        print("delay:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S"))
        redisClient.eval(delay_script.delayTaskLua, 1, config.ofrecordStartQueue, delayId, int(time.time()))
        schedule.enter(inc, 0, delaySchduled, (inc, redisClient))
    except Exception as e:
        print("delay error" + e)

def delayKeyThread(redisClient):
    """Delay task thread.
        Args:
            redisClient: redis client.
    """
    schedule.enter(0, 0, delaySchduled, (5, redisClient))
    schedule.run()