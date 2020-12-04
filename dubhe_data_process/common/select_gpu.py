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
import os
import sys
import pynvml
import logging

pid = os.getpid()
pynvml.nvmlInit()


def select_gpu():
    deviceCount = pynvml.nvmlDeviceGetCount()
    for i in range(deviceCount):
        logging.info('-------------get GPU information--------------')
        handle = pynvml.nvmlDeviceGetHandleByIndex(i)
        logging.info("Device:%s %s", i, pynvml.nvmlDeviceGetName(handle))
        gpu_info = pynvml.nvmlDeviceGetMemoryInfo(handle)
        logging.info('free:%s MB', gpu_info.free / (1000 * 1000))
        if gpu_info.free / (1000 * 1000) > 3072:
            os.environ["CUDA_VISIBLE_DEVICES"] = str(i)
            logging.info('use GPU:%s %s', i, pynvml.nvmlDeviceGetName(handle))
            return
    logging.info('No GPU is currently available')
    sys.exit()
