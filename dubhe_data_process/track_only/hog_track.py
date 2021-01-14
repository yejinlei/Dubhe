#!/usr/bin/env python3
# -*- coding: utf-8 -*-
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
import time
from datetime import datetime

import cv2
import numpy as np
from track_only.mot_track_kc import KCTracker
from track_only.util import draw_bboxes_conf_cls

#将box四点坐标转换成左上角坐标和宽和高，并过滤低置信度的框
def bbox_to_xywh_cls_conf(bbox_xyxyc, conf_thresh=0.5):
    if any(bbox_xyxyc[:, 4] >= conf_thresh):
        bbox = bbox_xyxyc[bbox_xyxyc[:, 4] >= conf_thresh, :]
        bbox[:, 2] = bbox[:, 2] - bbox[:, 0]  #
        bbox[:, 3] = bbox[:, 3] - bbox[:, 1]  #
        return bbox
    else:
        return []

#opencv显示目标框和置信度
def showResult(result_xyxyc, ori_im, save_pth, color=(0, 255, 0), conf_th=0.4):
    ori_im = ori_im.copy()
    result_xyxyc = result_xyxyc.copy()
    result_xyxyc = result_xyxyc[result_xyxyc[:, 4] > conf_th]
    for d in result_xyxyc:
        x, y, x2, y2, conf = d
        cv2.putText(ori_im, str(conf), (int(x), int(y + 15)),
                    cv2.FONT_HERSHEY_PLAIN, 1, color, 1)
        cv2.rectangle(ori_im, (int(x), int(y)),
                      (int(x2), int(y2)), (0, 255, 0), 1)
    cv2.imwrite(save_pth, ori_im)


def makeFile(file_pth):
    file_dir, file_name = os.path.split(file_pth)
    os.makedirs(file_dir, exist_ok=True)
    if os.path.exists(file_pth):
        os.remove(file_pth)


#跟踪处理类
class Detector(object):
    def __init__(
            self,
            vid_path,
            min_confidence=0.4,
            max_cosine_distance=0.2,
            max_iou_distance=0.7,
            max_age=30,
            out_dir='res/'):
        self.vdo = cv2.VideoCapture()
        self.out_dir = out_dir
        if not os.path.exists(out_dir):
            os.makedirs(out_dir)

        self.class_ = 80
        self.kc_tracker = []
        if self.class_ > 0:
            for cls_id in range(self.class_):
                kc_tracker = KCTracker(
                    confidence_l=0.01,
                    confidence_h=0.02,
                    use_filter=True,
                    max_cosine_distance=max_cosine_distance,
                    max_iou_distance=max_iou_distance,
                    max_age=max_age,
                    cls_=cls_id)
                self.kc_tracker.append(kc_tracker)
        else:
            print("class_ is error!")
            return None

        _, filename = os.path.split(vid_path)
        self.mot_txt = os.path.join(self.out_dir, filename[:-4] + '.txt')
        self.mot_txt_filter = os.path.join(
            self.out_dir, filename[:-4] + '_filter.txt')
        self.mot_txt_bk = os.path.join(self.out_dir, filename[:-4] + '_bk.txt')
        self.det_txt = os.path.join(self.out_dir, filename[:-4] + '_det.txt')
        self.video_name = os.path.join(
            self.out_dir, filename[:-4] + '_res.avi')
        self.features_npy = os.path.join(
            self.out_dir, filename[:-4] + '_det.npy')
        self.save_feature = False
        self.all_features = []
        self.write_det_txt = False
        self.write_video = False
        self.use_tracker = True
        self.person_id = 1
        self.write_img = True
        self.write_json = True
        self.read_json = True
        self.write_bk = False
        self.temp_dir = filename[:-4]
        if self.write_img:
            self.img_dir = os.path.join(
                self.out_dir + '/' + self.temp_dir, 'imgs')
            os.makedirs(self.img_dir, exist_ok=True)
        if self.write_json or self.read_json:
            self.json_dir = os.path.join(
                self.out_dir + '/' + self.temp_dir, 'json')
            if self.write_json:
                os.makedirs(self.json_dir, exist_ok=True)
        print("Track Detector init sucessed!\n")

    #打开视频，并创建结果视频
    def open(self, video_path):
        assert os.path.isfile(video_path), "Error: path error"
        print("video_path %s \n" % video_path)
        self.vdo.open(video_path)
        self.im_width = int(self.vdo.get(cv2.CAP_PROP_FRAME_WIDTH))
        self.im_height = int(self.vdo.get(cv2.CAP_PROP_FRAME_HEIGHT))
        self.fps = self.vdo.get(cv2.CAP_PROP_FPS)

        self.area = 0, 0, self.im_width, self.im_height
        if self.write_video:
            fourcc = cv2.VideoWriter_fourcc(*'XVID')
            self.output = cv2.VideoWriter(
                self.video_name, fourcc, self.fps, (self.im_width, self.im_height))
        if self.im_width > 0 and self.im_height > 0:
            print("open video sucessed!\n")

    #结果保存成json文件
    def save_file(self, path, item):
        item = json.dumps((item))
        try:
            if not os.path.exists(path):
                with open(path, "w", encoding='utf-8') as f:
                    f.write(item + "\n")
            else:
                with open(path, "w", encoding='utf-8') as f:
                    f.write(item + "\n")
        except Exception as e:
            print("write error==>", e)

    #从coco json文件中解析目标框
    def de_coco_format(self, ann_json):
        json_data = []
        # annotations = ann_json['annotation']
        if ann_json:
            for i, annotation in enumerate(ann_json):
                conf_ = float(annotation['score'])
                cls_ = int(annotation['category_id'] - 1 - 80)
                x1 = float(annotation['bbox'][0])
                x2 = float(annotation['bbox'][2] + x1)
                y1 = float(annotation['bbox'][1])
                y2 = float(annotation['bbox'][3] + y1)
                object_ = [x1, y1, x2, y2, conf_, cls_]
                json_data.append(np.array(object_))
        return np.array(json_data)

    #将目标框等信息保存成coco json文件
    def coco_format(self, type_, id_name, im, result):
        # annotations = []
        temp = []
        height, width, _ = im.shape
        if result.shape[0] == 0:
            return temp
        else:
            for j in range(result.shape[0]):
                cls_id = int(result[j][6]) + 1 + 80
                x1 = int(result[j][0])
                x2 = int(result[j][2])
                y1 = int(result[j][1])
                y2 = int(result[j][3])
                track_id = int(result[j][5])
                score = float(result[j][4])
                width = max(0, x2 - x1)
                height = max(0, y2 - y1)
                temp.append({
                    'category_id': cls_id,
                    'area': width * height,
                    'iscrowd': 0,
                    'bbox': [x1, y1, width, height],
                    'segmentation': [[x1, y1, x2, y1, x2, y2, x1, y2]],
                    'score': score,
                    'track_id': track_id
                })
        return temp

    #跟踪处理，这里用于调试
    def detect(self):
        xmin, ymin, xmax, ymax = self.area
        frame_no = 0
        avg_fps = 0.0
        # for path, img, ori_im, vid_cap in self.dataset:
        while True:
            ret, ori_im = self.vdo.read()
            if ori_im is None:
                break
            frame_no += 1
            start = time.time()
            im = ori_im[ymin:ymax, xmin:xmax]
            t1 = time.time()
            # results = self.yolo_detect.run(img, ori_im)
            results = []
            if self.write_json:
                jsonname = self.json_dir + '/' + str(frame_no) + '.json'
                if len(results) > 0:
                    ann = self.coco_format(1, 1, ori_im, results)
                    self.save_file(jsonname, ann)
            if self.read_json:
                jsonname = self.json_dir + '/' + str(frame_no) + '.json'
                # print("jsonname %s" %(jsonname))
                with open(jsonname, 'r', encoding='utf8')as fp:
                    ann_json = json.load(fp)
                    results = self.de_coco_format(ann_json)
            t2 = time.time()
            bbox_xywhcs = []
            if len(results) > 0:
                for cls_id in range(self.class_):
                    results_cls = np.array(results)
                    results_cls = results_cls[results_cls[:, 5] == cls_id]
                    results_cls_ = results_cls[:, [0, 1, 2, 3, 4]]
                    bbox_xywhcs = bbox_to_xywh_cls_conf(
                        results_cls_, conf_thresh=0.05)

                    if len(bbox_xywhcs) > 0:
                        outputs = []
                        features = []
                        feature_type = 0
                        if cls_id != 0:
                            feature_type = 1
                        if self.use_tracker:
                            outputs, features_ = self.kc_tracker[cls_id].update(
                                frame_no, bbox_xywhcs, ori_im, type=feature_type)
                            features.append(features_)
                            if self.save_feature:
                                if self.all_features is None:
                                    self.all_features = features
                                else:
                                    self.all_features = np.vstack(
                                        (self.all_features, features))

                        if len(outputs) > 0:
                            bbox_xyxy = outputs[:, 1:5]
                            identities = outputs[:, 0]
                            confs = outputs[:, -1]
                            ori_im = draw_bboxes_conf_cls(
                                ori_im, bbox_xyxy, confs, identities, offset=(
                                    xmin, ymin), cls_id_=cls_id)
            else:
                for cls_id in range(self.class_):
                    self.kc_tracker[cls_id].update(frame_no, bbox_xywhcs, im)
            end = time.time()
            fps = 1 / (end - start)
            avg_fps += fps
            if frame_no % 100 == 0:
                print(
                    "detect cost time: {}s, fps: {}, frame_no : {} track cost:{}".format(
                        end - start, fps, frame_no, end - t2))

            if self.write_video:
                self.output.write(ori_im)
            if self.write_img:
                cv2.imwrite(
                    os.path.join(
                        self.img_dir,
                        '{:06d}.jpg'.format(frame_no)),
                    ori_im)

        self.vdo.release()
        if self.save_feature:
            self.saveFeature(self.features_npy, self.all_features)

    #跟踪处理，用于自动标定
    def run_track(self, image_list, label_list):
        frame_no = 0
        avg_fps = 0.0
        # self.json_dir = label_json_path
        for image_id, image in enumerate(image_list):
            label_name = label_list[image_id]
            if not os.path.exists(image):
                return ('image_path_error:' + image)
            if not os.path.exists(label_name):
                return ('label_path_error:' + label_name)
            ori_im = cv2.imread(image)
            if ori_im is None:
                return ('image error:' + image)
            frame_no += 1
            print(frame_no)
            start = time.time()
            t1 = time.time()
            results = []

            if self.read_json:
                jsonname = str(label_name)
                with open(jsonname, 'r', encoding='utf8')as fp:
                    ann_json = json.load(fp)
                    results = self.de_coco_format(ann_json)
            t2 = time.time()
            bbox_xywhcs = []
            if len(results) > 0:
                outputs = []
                for cls_id in range(self.class_):
                    results_cls = np.array(results)
                    results_cls = results_cls[results_cls[:, 5] == cls_id]
                    results_cls_ = results_cls[:, [0, 1, 2, 3, 4]]
                    bbox_xywhcs = bbox_to_xywh_cls_conf(
                        results_cls_, conf_thresh=0.05)
                    if len(bbox_xywhcs) > 0:
                        output = []
                        features = []
                        feature_type = 1
                        if self.use_tracker:
                            output, features_ = self.kc_tracker[cls_id].update(
                                frame_no, bbox_xywhcs, ori_im, type=feature_type)
                            features.append(features_)
                            if self.save_feature:
                                if self.all_features is None:
                                    self.all_features = features
                                else:
                                    self.all_features = np.vstack(
                                        (self.all_features, features))
                        if len(output) > 0:
                            bbox_xyxy = output[:, 0:4]
                            identities = output[:, 5]
                            confs = output[:, 4]
                            ori_im = draw_bboxes_conf_cls(
                                ori_im, bbox_xyxy, confs, identities, offset=(
                                    0, 0), cls_id_=cls_id)
                            output = np.insert(
                                output, 6, values=cls_id, axis=1)
                            if len(outputs) == 0:
                                outputs = output
                            else:
                                outputs = np.vstack((outputs, output))
                if self.write_json:
                    jsonname = str(label_name)
                    if len(outputs) > 0:
                        ann = self.coco_format(1, 1, ori_im, outputs)
                        self.save_file(jsonname, ann)

            else:
                for cls_id in range(self.class_):
                    self.kc_tracker[cls_id].update(
                        frame_no, bbox_xywhcs, ori_im)
            end = time.time()
            fps = 1 / (end - start)
            avg_fps += fps

            if self.write_video:
                self.output.write(ori_im)
            if self.write_img:
                cv2.imwrite(
                    os.path.join(
                        self.img_dir,
                        '{:06d}.jpg'.format(frame_no)),
                    ori_im)

        self.vdo.release()
        if self.save_feature:
            self.saveFeature(self.features_npy, self.all_features)
        return 'OK'


if __name__ == "__main__":

    os.environ['CUDA_VISIBLE_DEVICES'] = '0'
    start_time = datetime.now()
    print('start time:', start_time)
    vid_path = 'demo.avi'
    filename = 'demo.avi'
    det = Detector(
        filename,
        min_confidence=0.35,
        max_cosine_distance=0.2,
        max_iou_distance=0.7,
        max_age=30,
        out_dir='results/xxx')
    det.save_feature = False
    det.write_det_txt = False
    det.use_tracker = True
    det.write_video = False
    det.write_bk = False
    image_path = 'test/'
    label_json_path = 'test-json/'
    image_list = os.listdir(image_path)
    image_list.sort(reverse=False)
    label_list = os.listdir(label_json_path)
    label_list.sort(reverse=False)
    image_list_ = []
    label_list_ = []
    for label_ in label_list:
        label_list_.append((label_json_path + label_))
    for image_ in image_list:
        image_list_.append((image_path + image_))
    det.run_track(image_list_, label_list_)
    end_time = datetime.now()
    print(' cost hour:', (end_time - start_time) / 3600)
