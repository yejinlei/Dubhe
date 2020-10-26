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
  Reference:
    - [Single Image Haze Removal Using Dark Channel Prior]
      (http://kaiminghe.com/publications/cvpr09.pdf) (CVPR 2009)
"""
# !/usr/bin/env python
# -*- coding:utf-8 -*-
import cv2
import numpy as np


def guidedFilter(I, p, r, eps):
    """The implementation of guide filter
        Args:
          I: Guide image
          p: Input image
          r: The radius of filter window
          eps: Regularization parameter
        Returns:
          re: The result of guide filter
    """
    mean_I = cv2.boxFilter(I, -1, (r, r))
    mean_p = cv2.boxFilter(p, -1, (r, r))
    mean_Ip = cv2.boxFilter(I * p, -1, (r, r))
    cov_Ip = mean_Ip - mean_I * mean_p

    mean_II = cv2.boxFilter(I * I, -1, (r, r))
    var_I = mean_II - mean_I * mean_I

    a = cov_Ip / (var_I + eps)
    b = mean_p - a * mean_I

    mean_a = cv2.boxFilter(a, -1, (r, r))
    mean_b = cv2.boxFilter(b, -1, (r, r))
    re = mean_a * I + mean_b
    return re


def AtmLight(img, TR, bins=2000):
    """Get the global atmospheric light of input image
        Args:
          img: Input image
          TR: The refined atmospheric mask image
          bins: The number of equal-width bins in the given range
        Returns:
          A: The global atmospheric light of input image
    """
    ht = np.histogram(TR, bins)
    d = np.cumsum(ht[0]) / float(TR.size)
    try:
        lmax = next(y for y in range(len(d) - 1, 0, -1) if d[y] <= 0.999)
    except:
        lmax = 1
    A = np.mean(img, 2)[TR >= ht[1][lmax]].max()
    return A


def TransRefine(img, radius, eps, dehaze_ratio, maxTR):
    """Get the refined atmospheric mask image
        Args:
          img: Input image
          radius: The radius of filter window for guide filter
          eps: The radius of filter window for guide filter
          dehaze_ratio: the ratio of dehaze
          maxTR: The limitation of the output
        Returns:
          TR: The refined atmospheric mask image
    """
    h, w = img.shape[:2]
    img = cv2.pyrDown(img, (w // 4, h // 4))
    TR = np.min(img, 2)
    filter_TR = cv2.erode(TR, np.ones((2 * radius + 1, 2 * radius + 1)))
    TR = guidedFilter(TR, filter_TR, radius, eps)
    TR = cv2.resize(TR, (w, h))
    TR = np.minimum(TR * dehaze_ratio, maxTR)
    return TR


def deHaze(
        img,
        radius=81,
        eps=0.001,
        dehaze_ratio=0.95,
        maxTR=0.80):
    re = np.zeros(img.shape)
    TR = TransRefine(img, radius, eps, dehaze_ratio, maxTR)
    A = AtmLight(img, TR, bins=2000)
    for k in range(3):
        re[:, :, k] = (img[:, :, k] - TR) / (1 - TR / A)
    re = np.clip(re, 0, 1)
    return re


def addHaze(img, radius=81, eps=0.001, dehaze_ratio=0.95, maxTR=0.80):
    re = np.zeros(img.shape)
    TR = TransRefine(img, radius, eps, dehaze_ratio, maxTR)
    A = AtmLight(img, TR, bins=2000)
    for k in range(3):
        re[:, :, k] = (img[:, :, k] * 0.7) + A * 0.3
    re = np.clip(re, 0, 1)
    return re
