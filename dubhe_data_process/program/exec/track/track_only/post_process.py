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
import os
from collections import defaultdict

import numpy as np


def removeSmallOrBigBbox(
        fid_pid_tlwhc,
        area_min_scale=0.25,
        area_max_scale=1.75,
        conf_thresh=0.95,
        cp_count_thresh=100,
        height=1080,
        split=10,
        show_delete_items=False):
    selected_ind = []
    filtered_ind = []

    ys = np.linspace(0, height, split + 1).astype(np.int)

    y_range2cps = {}
    for i in range(len(ys) - 1):
        y_range2cps[(ys[i], ys[i + 1])] = []

    filter_info = defaultdict(list)
    filter_counter = 0
    person_id2count = defaultdict(int)

    for ind, (frame_id, person_id, x, y, w, h,
              conf) in enumerate(fid_pid_tlwhc):
        if (ind + 1) % (len(fid_pid_tlwhc) // 10) == 0:
            print('filtering %d/%d...' % (ind + 1, len(fid_pid_tlwhc)))
        cp_x, cp_y = x + w / 2, y + h / 2
        for (y0, y1), cps in y_range2cps.items():
            if y0 <= cp_y <= y1:
                # print('y0: {}, y1: {}, cp_y: {}'.format(y0, y1, cp_y))
                # print((y0, y1), cps)
                if len(cps) >= cp_count_thresh:
                    cps_arr = np.array(cps)
                    area_mean = np.mean(cps_arr[:, 3])
                    # sorted_area_ind = np.argsort(cps_arr[:, 3])
                    # sorted_areas = cps_arr[:, 3][sorted_area_ind]
                    # area_min = sorted_areas[int(len(sorted_areas) * 0.05)]
                    # area_max = sorted_areas[int(len(sorted_areas) * 0.95)]
                    area_min = area_mean * area_min_scale
                    area_max = area_mean * area_max_scale
                    # print('area_per_20: {}, area_per_80: {}, area: {}'.format(area_per_20, area_per_80, w*h))
                    if area_min <= w * \
                            h <= area_max or conf > conf_thresh:  # or person_id2count[person_id] < 10
                        person_id2count[person_id] += 1
                        cps.append([person_id, w, h, w * h])
                        selected_ind.append(ind)
                    else:
                        filter_info[frame_id].append(person_id)
                        filter_counter += 1
                        filtered_ind.append(ind)
                        # print('filter line: %d, frame id: %d, person id: %d' % (ind+1, frame_id, person_id))
                else:
                    cps.append([person_id, w, h, w * h])
                    selected_ind.append(ind)
                break
    # print('delete totally %d items' % filter_counter)
    result = []
    for ind, data in enumerate(fid_pid_tlwhc):
        if ind in selected_ind:
            result.append(data)
    if show_delete_items:
        for f_id, p_ids in filter_info.items():
            print('filter frame id: {}, person ids: {}'.format(f_id, p_ids))
    return np.array(result)


def writeResult(fid_pid_bbox_xywhc, save_file_pth, with_conf=False):
    with open(save_file_pth, 'w') as f:
        frame_max = int(np.max(fid_pid_bbox_xywhc[:, 0]))
        for frame_id in range(frame_max):
            frame_data = fid_pid_bbox_xywhc[fid_pid_bbox_xywhc[:, 0]
                                            == frame_id + 1]
            tempdata = frame_data[np.argsort(frame_data[:, 1])]
            for d in tempdata:
                if with_conf:
                    msg = '%d,%d,%.2f,%.2f,%.2f,%.2f,%.3f\n' % (
                        d[0], d[1], d[2], d[3], d[4], d[5], d[6])
                else:
                    msg = '%d,%d,%.2f,%.2f,%.2f,%.2f\n' % (
                        d[0], d[1], d[2], d[3], d[4], d[5])
                f.write(msg)


def seleteDateByIndex(index_, data):
    temp = []
    for pid in index_:
        temp.append(data[data[:, 1] == pid])
    res = np.vstack(temp)
    return res


def removeDateByIndex(index_, data):
    temp = []
    # index_ = index_.tolist()
    for item in data:
        if np.any(item[1] == index_):
            continue
        temp.append(item)
    res = np.vstack(temp)
    return res


def printResult(bbox_xyxyc):
    for i, d in enumerate(bbox_xyxyc):
        pid, conf_max, conf_mean, mid_point_stdx, mid_point_stdy, start_frame, frame_len = d
        print(
            'pid:{:.0f},cof_max:{:.2f},conf_mean:{:.2f},mid_point_stdx:{:.2f},mid_point_stdy:{:.2f},start_frame:{:.0f},frame_len:{:.0f}'.format(
                pid,
                conf_max,
                conf_mean,
                mid_point_stdx,
                mid_point_stdy,
                start_frame,
                frame_len))


def getMindPointArray(xywhcs_np):
    xywhcs_np_temp = xywhcs_np.copy()
    cxcy = xywhcs_np_temp[:, 1:3] + xywhcs_np_temp[:, 3:4] / 2
    return cxcy


def getTrackItemInfo(track_data):
    res = []
    pid_max = int(np.max(data[:, 1]))
    for pid in range(pid_max):
        data_p = data[data[:, 1] == pid]
        if len(data_p) > 0:
            confs = data_p[:, -1]
            conf_max = np.max(confs)
            conf_mean = np.mean(confs)
            start_frame = np.min(data_p[:, 0])
            cxcy = getMindPointArray(data_p)
            mid_point_std = np.std(cxcy, axis=0)
            frame_len = len(data_p)
            res.append([pid, conf_max, conf_mean, mid_point_std[0],
                        mid_point_std[1], start_frame, frame_len])
            print(
                'pid:{:d},cof_max:{:.2f},conf_mean:{:.2f},mid_point_std:{},start_frame:{:.0f},frame_len:{:d}'.format(
                    pid,
                    conf_max,
                    conf_mean,
                    mid_point_std,
                    start_frame,
                    frame_len))
    res = np.array(res)
    return res


def getUnMoveLowConfObjInfo(fid_pid_tlwhc, std_th=1., conf_th=0.5):
    res = []
    pid_max = int(np.max(fid_pid_tlwhc[:, 1]))
    for pid in range(pid_max):
        data_p = fid_pid_tlwhc[fid_pid_tlwhc[:, 1] == pid]
        if len(data_p) > 0:
            confs = data_p[:, -1]
            conf_max = np.max(confs)
            conf_mean = np.mean(confs)
            start_frame = np.min(data_p[:, 0])
            cxcy = getMindPointArray(data_p)
            mid_point_std = np.std(cxcy, axis=0)
            frame_len = len(data_p)
            res.append([pid, conf_max, conf_mean, mid_point_std[0],
                        mid_point_std[1], start_frame, frame_len])
            print(
                'pid:{:d},cof_max:{:.2f},conf_mean:{:.2f},mid_point_std:{},start_frame:{:.0f},frame_len:{:d}'.format(
                    pid,
                    conf_max,
                    conf_mean,
                    mid_point_std,
                    start_frame,
                    frame_len))
    res = np.array(res)
    res_conf_le_0_5_T = res[(res[:, 2] < conf_th) & (
        res[:, 3] < std_th) & (res[:, 4] < std_th)]
    return res_conf_le_0_5_T


def removeUnMoveLowConfObj(fid_pid_tlwhc, std_th=1, conf_th=0.5):
    res_info = []
    pid_max = int(np.max(fid_pid_tlwhc[:, 1]))
    for pid in range(pid_max):
        data_p = fid_pid_tlwhc[fid_pid_tlwhc[:, 1] == pid]
        if len(data_p) > 0:
            confs = data_p[:, -1]
            conf_max = np.max(confs)
            conf_mean = np.mean(confs)
            start_frame = np.min(data_p[:, 0])
            cxcy = getMindPointArray(data_p)
            mid_point_std = np.std(cxcy, axis=0)
            frame_len = len(data_p)
            res_info.append([pid,
                             conf_max,
                             conf_mean,
                             mid_point_std[0],
                             mid_point_std[1],
                             start_frame,
                             frame_len])
            # print('pid:{:d},cof_max:{:.2f},conf_mean:{:.2f},mid_point_std:{},start_frame:{:.0f},frame_len:{:d}'.format(
            # pid, conf_max, conf_mean, mid_point_std, start_frame, frame_len))
    res = np.array(res_info)
    res_conf_le_0_5 = res[(res[:, 2] < conf_th) & (
        res[:, 3] < std_th) & (res[:, 4] < std_th)]
    remove_inds = res_conf_le_0_5[:, 0]
    selet_data = removeDateByIndex(remove_inds, fid_pid_tlwhc)
    return selet_data


if __name__ == "__main__":
    result = './xxx.txt'
    res_dir, res_name = os.path.split(result)
    data = np.loadtxt(result, delimiter=',')
    res1 = getTrackItemInfo(data)
    res2 = getUnMoveLowConfObjInfo(data)
    res3 = removeUnMoveLowConfObj(data)

    print('##################################')
    printResult(res1)
    print('##################################')
    printResult(res2)

    data_conf_le0_5 = seleteDateByIndex(res1[:, 0], data)
    writeResult(
        data_conf_le0_5,
        os.path.join(
            res_dir,
            'A1_' +
            res_name),
        with_conf=True)
    data_conf_le0_5 = seleteDateByIndex(res2[:, 0], data)
    writeResult(
        data_conf_le0_5,
        os.path.join(
            res_dir,
            'A2_' +
            res_name),
        with_conf=True)
