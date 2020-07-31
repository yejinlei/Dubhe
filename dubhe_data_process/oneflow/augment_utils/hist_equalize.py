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

import cv2
import numpy as np


def hist_equalize(img):
    """The implementation of histgram equalization in channel L"""
    img_HLS = cv2.cvtColor(img, cv2.COLOR_BGR2HLS)
    h, l, s = cv2.split(img_HLS)
    lH = cv2.equalizeHist(l)
    temp = cv2.merge((h, lH, s))
    re = cv2.cvtColor(temp, cv2.COLOR_HLS2BGR)
    return re


def YUV_hist_equalize(img):
    """The implementation of histgram equalization in channel YUV"""
    imgYUV = cv2.cvtColor(img, cv2.COLOR_BGR2YCrCb)
    channelYUV = cv2.split(imgYUV)
    channelYUV[0] = cv2.equalizeHist(channelYUV[0])
    channels = cv2.merge(channelYUV)
    re = cv2.cvtColor(channels, cv2.COLOR_YCrCb2BGR)
    return re


def adaptive_hist_equalize(img):
    """The implementation of constrast limited adaptive histgram equalization"""
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    imgYUV = cv2.cvtColor(img, cv2.COLOR_BGR2YCrCb)
    channelYUV = cv2.split(imgYUV)
    channelYUV[0] = clahe.apply(channelYUV[0])
    channels = cv2.merge(channelYUV)
    re = cv2.cvtColor(channels, cv2.COLOR_YCrCb2BGR)
    return re


def log_trans(img):
    img_out = 42 * np.log(1.0 + img)
    img_out = np.uint8(img_out + 0.5)
    return img_out

