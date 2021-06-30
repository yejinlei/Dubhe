"""
MIT License
Copyright (c) 2020 Ziqiang
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
"""
import os
import time

import cv2
import numpy as np
from program.exec.track.track_only.feature.feature_extractor_batch import Extractor
from program.exec.track.track_only.post_process import removeUnMoveLowConfObj, writeResult, removeSmallOrBigBbox
from program.exec.track.track_only.sort.detection import Detection
from program.exec.track.track_only.sort.iou_matching import iou
from program.exec.track.track_only.sort.nn_matching import NearestNeighborDistanceMetric
from program.exec.track.track_only.sort.tracker import Tracker


class KCTracker(object):
    def __init__(
            self,
            model_path='torchreid/checkpoint/resnet50_model_20.pth',
            gpu_ids='0',
            model_name='osnet_x1_0',
            confidence_l=0.2,
            confidence_h=0.4,
            max_cosine_distance=0.2,
            max_iou_distance=0.7,
            save_feature=False,
            use_filter=False,
            init_extractor=True,
            max_age=30,
            std_Q_w=1e-1,
            std_Q_wv=1e-3,
            std_R_w=5e-2,
            cls_=0):
        self.confidence_l = confidence_l
        self.confidence_h = confidence_h
        self.iou_thresh_l = 0.24
        self.iou_thresh = 0.5
        self.nms_max_overlap = 1.0
        self.extractor = None
        self.height, self.width = None, None
        if init_extractor:
            self.extractor = Extractor(model_name=model_name,
                                       load_path=model_path,
                                       gpu_ids=gpu_ids, cls=cls_)
        max_iou = max_iou_distance
        nn_budget = 100
        metric = NearestNeighborDistanceMetric(
            "cosine", max_cosine_distance, nn_budget)
        self.tracker = Tracker(
            metric,
            max_iou_distance=max_iou,
            max_age=max_age,
            std_Q_w=std_Q_w,
            std_Q_wv=std_Q_wv,
            std_R_w=std_R_w)
        self.all_feature = None
        self.save_feature = save_feature
        self.count = 1
        self.result = []
        self.use_filter = use_filter
        #print('batch mode')

    def saveResult(self, file_name):
        if os.path.exists(file_name):
            os.remove(file_name)
        self.result = np.array(self.result)  # frameid_pid_tlwhc
        if self.use_filter:
            self.result = removeUnMoveLowConfObj(self.result)
        else:
            self.result = removeSmallOrBigBbox(self.result)
        writeResult(self.result, file_name)
        print('save result:', file_name)

    def getFeatureFromImage(self, bbox_tlwhcs, data, input_type, type):
        bbox_tlwhs = bbox_tlwhcs[:, 0:4]
        features = None
        if input_type == 'img':
            self.height, self.width = data.shape[:2]
            try:
                features = self._get_features_batch(bbox_tlwhs, data, type)
            except Exception as e:
                print(e)
        else:  # input_type == 'feature'
            features = data
        return features

    def update(self, frame_id, bbox_tlwhcs, ori_img, input_type='img', type=0):

        #print('ini boxs number:',len(bbox_tlwhcs))
        # print('ini confs number:',len(confidences))
        if len(bbox_tlwhcs) == 0:
            self.count += 1
            return [], []
        confidences = bbox_tlwhcs[:, -1]
        mask_l = (
            confidences >= self.confidence_l) & (
            confidences < self.confidence_h)
        mask_h = confidences >= self.confidence_h
        bbox_tlwhcs_low = bbox_tlwhcs[mask_l, :]
        bbox_tlwhcs_ture = bbox_tlwhcs[mask_h, :]

        bbox_tlwhcs_new = []
        bbox_tlwhcs_temp = bbox_tlwhcs_low.copy()
        for track in self.tracker.tracks:
            if not track.is_confirmed() or track.time_since_update > 1:
                continue
            if len(bbox_tlwhcs_temp) == 0:
                continue
            box_tlwh_temp = track.to_tlwh()
            ious_ = iou(box_tlwh_temp, bbox_tlwhcs_temp[:, 0:4])
            iou_max_ind = np.argmax(ious_)
            if ious_[iou_max_ind] > self.iou_thresh_l:
                bbox_tlwhcs_new.append(bbox_tlwhcs_temp[iou_max_ind])
                np.delete(bbox_tlwhcs_temp, iou_max_ind, axis=0)

        bbox_tlwhcs_new = np.array(bbox_tlwhcs_ture.tolist() + bbox_tlwhcs_new)
        if len(bbox_tlwhcs_new) == 0:
            self.count += 1
            return [], []
        # try:
        #    indices = non_max_suppression(bbox_tlwhcs_new[:, 0:4], 0.6, bbox_tlwhcs_new[:, 4])
        #    bbox_tlwhcs_new = np.array([bbox_tlwhcs_new[i] for i in indices])
        # except Exception as e:
        #    print(e)
        #    return [], []
        if len(bbox_tlwhcs_new) == 0:
            self.count += 1
            return [], []
        bbox_tlwhs_new = bbox_tlwhcs_new[:, 0:4]
        confidences_new = bbox_tlwhcs_new[:, 4]

        features = self.getFeatureFromImage(
            bbox_tlwhcs_new, ori_img, input_type, type)

        if self.save_feature:
            if self.all_feature is None and len(features):
                self.all_feature = features
            else:
                self.all_feature = np.vstack((self.all_feature, features))

        detections = [
            Detection(
                bbox_tlwhs_new[i],
                conf,
                features[i],
                i) for i,
            conf in enumerate(confidences_new)]
        # update tracker
        self.tracker.predict()
        self.tracker.update(detections, self.confidence_h)
        self.count += 1

        # output bbox identities
        outputs = []
        for track in self.tracker.tracks:
            if not track.is_confirmed() or track.time_since_update > 1:
                continue
            box_tlwh = track.to_tlwh()  # tlwh
            x1, y1, x2, y2 = self._tlwh_to_xyxy(box_tlwh)
            track_id = track.track_id
            conf = track.confidence
            ori_id = track.ori_id
            outputs.append(np.array([track_id, x1, y1, x2, y2, conf, ori_id]))
            self.result.append(
                np.array([frame_id, track_id, x1, y1, box_tlwh[2], box_tlwh[3], conf]))
        bbox_tlwhcs_results = []
        for i, bbox in enumerate(bbox_tlwhcs):
            track_id_ = -1
            for output in outputs:
                if int(output[6]) == i:
                    track_id_ = output[0]
            # if track_id_ == -1:
            #    continue
            box_tlwh = bbox[0:4]
            conf_ = bbox[4]
            x1, y1, x2, y2 = self._tlwh_to_xyxy(box_tlwh)
            bbox_tlwhcs_results.append(
                np.array([x1, y1, x2, y2, conf_, track_id_]))

        if len(bbox_tlwhcs_results) > 0:
            bbox_tlwhcs_results = np.stack(bbox_tlwhcs_results, axis=0)

        return bbox_tlwhcs_results, features

    # for centernet (x1,x2 w,h -> x1,y1,x2,y2)
    def _tlwh_to_xyxy(self, bbox_tlwh):
        x1, y1, w, h = bbox_tlwh
        x2 = x1 + w
        y2 = y1 + h
        return x1, y1, x2, y2

    def _tlwh_to_limit_xyxy(self, bbox_tlwh):
        x1, y1, w, h = bbox_tlwh
        x1 = max(x1, 0)
        y1 = max(y1, 0)
        x2 = min(int(x1 + w), self.width - 1)
        y2 = min(int(y1 + h), self.height - 1)
        return int(x1), int(y1), x2, y2

    # for yolo  (centerx,centerx, w,h -> x1,y1,x2,y2)
    def _cxcywh_to_xyxy(self, bbox_xywh):
        x, y, w, h = bbox_xywh
        x1 = max(int(x - w / 2), 0)
        x2 = min(int(x + w / 2), self.width - 1)
        y1 = max(int(y - h / 2), 0)
        y2 = min(int(y + h / 2), self.height - 1)
        return x1, y1, x2, y2

    def _get_features_batch(self, bbox_tlwhs, ori_img, type):
        imgs = []
        if self.width is None:
            self.height, self.width = ori_img.shape[:2]
        for box in bbox_tlwhs:
            x1, y1, x2, y2 = self._tlwh_to_limit_xyxy(box)
            im = ori_img[int(y1):int(y2), int(x1):int(x2)]
            imgs.append(im)
        features = self.extractor(imgs, 20, feature_type=type)
        return features

    def _get_features(self, bbox_tlwh, ori_img):
        features = []
        if self.width is None:
            self.height, self.width = ori_img.shape[:2]
        for box in bbox_tlwh:
            x1, y1, x2, y2 = self._tlwh_to_limit_xyxy(box)
            im = ori_img[int(y1):int(y2), int(x1):int(x2)]
            feature = self.extractor(im)[0]
            features.append(feature)
        if len(features):
            features = np.stack(features, axis=0)
        else:
            features = np.array([])
        return features

    def saveFeature(self, filename=None):
        if filename is not None:
            np.save(filename, self.all_feature)
            print('save feature:', filename)


def genFeatureByDetectionBox(det_txt, img_dir, mot_tracker, conf_thresh=0.2):
    det_sequence = np.loadtxt(det_txt, delimiter=',')
    det_sequence = det_sequence[det_sequence[:, 6] > conf_thresh, :]
    out_file = det_txt[:-4] + '.npy'
    file_dir, file_name = os.path.split(det_txt)
    new_det_txt = os.path.join(file_dir, 'new_' + file_name)
    if os.path.exists(out_file):
        os.remove(out_file)
    frame_max = int(det_sequence[:, 0].max())
    all_features = []
    for frame_id in range(frame_max):
        frame_id += 1
        img_name = "{:04d}.jpg".format(frame_id)
        img_pth = os.path.join(img_dir, img_name)
        try:
            image = cv2.imread(img_pth)
        except Exception as e:
            image = None
            print(e)
        sel_ind = det_sequence[:, 0] == frame_id
        bbox_xywh = det_sequence[sel_ind, 2:6]
        if len(bbox_xywh) == 0:
            continue
        t1 = time.time()
        features = mot_tracker._get_features_batch(bbox_xywh, image)
        t2 = time.time()
        if len(all_features) == 0:
            all_features = features
        else:
            all_features = np.vstack((all_features, features))
        if frame_id % 100 == 0:
            print("frame_no:{},track cost time: {}s".format(frame_id, t2 - t1))

    np.save(out_file, all_features)
    print('save feature:', out_file)
    for i, d in enumerate(det_sequence):
        with open(new_det_txt, 'a') as f:
            msg = '%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n' % (
                d[0], d[1], d[2], d[3], d[4], d[5], d[6])
            f.write(msg)
