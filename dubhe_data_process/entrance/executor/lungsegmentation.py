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
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sched
import sys

sys.path.append("../../")
import logging
import time
import json
import numpy as np
import luascript.delaytaskscript as delay_script
import common.config as config
from datetime import datetime
from skimage.morphology import disk, binary_erosion, binary_closing
from skimage.measure import label,regionprops, find_contours
from skimage.filters import roberts
from scipy import ndimage as ndi
from skimage.segmentation import clear_border
import pydicom as dicom
import os
import logging

schedule = sched.scheduler(time.time, time.sleep)

base_path = "/nfs/"
delayId = ""


def process(task_dict, key):
    """Lung segmentation based on dcm task method.
        Args:
            task_dict: imagenet task details.
            key: imagenet task key.
    """
    global delayId
    delayId = "\"" + eval(str(key, encoding="utf-8")) + "\""
    task_dict = json.loads(task_dict)
    base_path = task_dict["annotationPath"]
    if not os.path.exists(base_path):
        logging.info("make annotation path.")
        os.makedirs(base_path)
    
    for dcm in task_dict["dcms"]:
        image, image_path = preprocesss_dcm_image(dcm)
        # segmentation and wirte coutours to result_path
        result_path = os.path.join(base_path, image_path)
        contour(segmentation(image), result_path)
    
    logging.info("all dcms in one task are processed.")
    return True

def preprocesss_dcm_image(path):
    """Load and preprocesss dcm image.
        Args:
            path: dcm file path.
    """
    # result_path = os.path.basename(path).split(".", 1)[0] + ".json"
    result_path = ".".join(os.path.basename(path).split(".")[0:-1]) + ".json"
    dcm = dicom.dcmread(path)
    image = dcm.pixel_array.astype(np.int16)
    
    # Set outside-of-scan pixels to 0.
    image[image == -2000] = 0

    # Convert to Hounsfield units (HU)
    intercept = dcm.RescaleIntercept
    slope = dcm.RescaleSlope

    if slope != 1:
        image = slope * image.astype(np.float64)
        image = image.astype(np.int16)

    image += np.int16(intercept)
    logging.info("preprocesss_dcm_image done.")
    return np.array(image, dtype=np.int16), result_path

def segmentation(image):
    """Segments the lung from the given 2D slice.
        Args:
            image: single image in one dcm.
    """
    # Step 1: Convert into a binary image.
    binary = image < -350

    # Step 2: Remove the blobs connected to the border of the image. 
    cleared = clear_border(binary)

    # Step 3: Label the image. 
    label_image = label(cleared)

    # Step 4: Keep the labels with 2 largest areas. 
    areas = [r.area for r in regionprops(label_image)]
    areas.sort()
    if len(areas) > 2:
        for region in regionprops(label_image):
            if region.area < areas[-2]:
                for coordinates in region.coords:
                    label_image[coordinates[0], coordinates[1]] = 0
    binary = label_image > 0

    # Step 5: Erosion operation with a disk of radius 2. This operation is seperate the lung nodules attached to the blood vessels.
    selem = disk(1)
    binary = binary_erosion(binary, selem)
    
    # Step 6: Closure operation with a disk of radius 10. This operation is to keep nodules attached to the lung wall. 
    selem = disk(16)
    binary = binary_closing(binary, selem)

    # Step 7: Fill in the small holes inside the binary mask of lungs.
    for _ in range(3):
        edges = roberts(binary)
        binary = ndi.binary_fill_holes(edges)
    logging.info("lung segmentation done.")
    return binary

def contour(image, path):
    """Get contours of segmentation.
        Args:
            seg: segmentation of lung.
    """
    result = []
    contours = find_contours(image, 0.5)
    if len(contours) > 2:
        contours.sort(key = lambda x: int(x.shape[0]))
        contours = contours[-2:]

    for n, contour in enumerate(contours):
        # result.append({"type":n, "annotation":contour.tolist()})
        result.append({"type":n, "annotation":np.flip(contour, 1).tolist()})
    
    # write json
    with open(path, 'w') as f:
        json.dump(result, f)
    logging.info("write {} done.".format(path))


def delaySchduled(inc, redisClient):
    """Delay task method.
        Args:
            inc: scheduled task time.
            redisClient: redis client.
    """
    try:
        print("delay:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S"))
        redisClient.eval(delay_script.delayTaskLua, 1, config.dcmStartQueue, delayId, int(time.time()))
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
