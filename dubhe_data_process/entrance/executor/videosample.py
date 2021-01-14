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
import json
import os
import sched
import time
from datetime import datetime

import luascript.finishtaskscript as finish_script
import luascript.failedtaskscript as failed_script
import luascript.delaytaskscript as delay_script
import common.config as config

import cv2

schedule = sched.scheduler(time.time, time.sleep)

datasetIdKey = ""


def sampleProcess(datasetId, path, frameList, redisClient):
    """Video sampling method.
        Args:
          datasetId: dataset id.
          path: video file path.
          frameList: picture frame number list.
          redisClient: redis client.
    """
    global datasetIdKey
    datasetIdJson = {'datasetIdKey': datasetId}
    datasetIdKey = json.dumps(datasetIdJson, separators=(',', ':'))
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
        for i in frameList:
            cap.set(cv2.CAP_PROP_POS_FRAMES, i)
            success, video_capture = cap.read()
            # 保存图片
            if success is True and video_capture is not None:
                save_name = save_path + videoName.split('.')[0] + '_' + str(i) + '.jpg'
                cv2.imwrite(save_name, video_capture)
                redisClient.lpush("videoSample_pictures:" + datasetId,
                                  '{' + '\"pictureName\":' + "\"" + save_name + "\"" + '}')
                print('image of %s is saved' % save_name)
        print('video is all read')
        redisClient.eval(finish_script.finishTaskLua, 3, config.videoStartQueue, config.videoFinishQueue,
                         "videoSample:" + str(datasetId),
                         datasetIdKey, str(datasetIdKey))
    except Exception as e:
        print(e)
        redisClient.eval(failed_script.failedTaskLua, 4, config.videoStartQueue, config.videoFailedQueue,
                         "videoSample_pictures:" + datasetId,
                         "videoSample:" + str(datasetId),
                         datasetIdKey, str(datasetIdKey))


def delaySchduled(inc, redisClient):
    """Delay task method.
        Args:
          inc: scheduled task time.
          redisClient: redis client.
    """
    try:
        print("delay:" + datetime.now().strftime("B%Y-%m-%d %H:%M:%S"))
        redisClient.eval(delay_script.delayTaskLua, 1, config.videoStartQueue, datasetIdKey, int(time.time()))
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
