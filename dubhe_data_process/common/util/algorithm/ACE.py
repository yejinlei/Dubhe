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
import cv2
import numpy as np
import math

para = {}


def ACE(img, ratio=4, radius=300):
    """The implementation of ACE"""
    global para
    para_mat = para.get(radius)
    if para_mat is not None:
        pass
    else:
        size = radius * 2 + 1
        para_mat = np.zeros((size, size))
        for h in range(-radius, radius + 1):
            for w in range(-radius, radius + 1):
                if not h and not w:
                    continue
                para_mat[radius + h, radius + w] = 1.0 / \
                                                   math.sqrt(h ** 2 + w ** 2)
        para_mat /= para_mat.sum()
        para[radius] = para_mat
    h, w = img.shape[:2]
    p_h, p_w = [0] * radius + list(range(h)) + [h - 1] * radius, \
               [0] * radius + list(range(w)) + [w - 1] * radius
    temp = img[np.ix_(p_h, p_w)]
    res = np.zeros(img.shape)
    for i in range(radius * 2 + 1):
        for j in range(radius * 2 + 1):
            if para_mat[i][j] == 0:
                continue
            res += (para_mat[i][j] *
                    np.clip((img - temp[i:i + h, j:j + w]) * ratio, -1, 1))
    return res


def ACE_channel(img, ratio, radius):
    """The implementation of ACE through individual channel"""
    h, w = img.shape[:2]
    if min(h, w) <= 2:
        return np.zeros(img.shape) + 0.5
    down_ori = cv2.pyrDown(img, ((w + 1) // 2, (h + 1) // 2))
    temp = ACE_channel(down_ori, ratio, radius)
    up_temp = cv2.resize(temp, (w, h))
    up_ori = cv2.resize(down_ori, (w, h))
    re = up_temp + ACE(img, ratio, radius) - ACE(up_ori, ratio, radius)
    return re


def ACE_color(img, ratio=4, radius=3):
    """Enhance the image through RGB channels"""
    re = np.zeros(img.shape)
    for c in range(3):
        re[:, :, c] = reprocessImage(ACE_channel(img[:, :, c], ratio, radius))
    return re


def reprocessImage(img):
    """Reprocess and map the image to [0,1]"""
    ht = np.histogram(img, 2000)
    d = np.cumsum(ht[0]) / float(img.size)
    try:
        left = next(x for x in range(len(d)) if d[x] >= 0.005)
    except:
        left = 1999
    try:
        right = next(y for y in range(len(d) - 1, 0, -1) if d[y] <= 0.995)
    except:
        right = 1
    return np.clip((img - ht[1][left]) / (ht[1][right] - ht[1][left]), 0, 1)
