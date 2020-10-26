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
# -*- coding:utf-8 -*-

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import sys
import codecs
import os
import numpy as np
from PIL import Image
import oneflow as flow
from of_model.resnet_model import resnet50

sys.stdout = codecs.getwriter("utf-8")(sys.stdout.detach())

def init_resnet():
    """Initialize ResNet with pretrained weights"""
    model_load_dir = 'of_model/resnet_v15_of_best_model_val_top1_773/'
    assert os.path.isdir(model_load_dir)
    check_point = flow.train.CheckPoint()
    check_point.load(model_load_dir)


def load_image(image_path):
    """Load and preprocess the image"""
    rgb_mean = [123.68, 116.779, 103.939]
    rgb_std = [58.393, 57.12, 57.375]
    im = Image.open(image_path).convert('RGB')
    im = im.resize((224, 224))
    im = np.array(im).astype('float32')
    im = (im - rgb_mean) / rgb_std
    im = np.transpose(im, (2, 0, 1))
    im = np.expand_dims(im, axis=0)
    return np.ascontiguousarray(im, 'float32')


@flow.global_function(flow.function_config())
def InferenceNet(images=flow.FixedTensorDef(
        (1, 3, 224, 224), dtype=flow.float)):
    """Run the inference of ResNet"""
    logits = resnet50(images, training=False)
    predictions = flow.nn.softmax(logits)
    return predictions


def resnet_inf(image_path):
    """The whole procedure of inference of ResNet and return the category_id and the corresponding score"""
    image = load_image(image_path.encode('utf-8'))
    predictions = InferenceNet(image).get()
    clsidx = predictions.ndarray().argmax() + 161
    return predictions.ndarray().max(), clsidx
