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
from __future__ import absolute_import

import numpy as np

from . import iou_matching
from . import kalman_filter
from . import linear_assignment
from .track import Track


class Tracker:
    """
    This is the multi-target tracker.

    Parameters
    ----------
    metric : nn_matching.NearestNeighborDistanceMetric
        A distance metric for measurement-to-track association.
    max_age : int
        Maximum number of missed misses before a track is deleted.
    n_init : int
        Number of consecutive detections before the track is confirmed. The
        track state is set to `Deleted` if a miss occurs within the first
        `n_init` frames.

    Attributes
    ----------
    metric : nn_matching.NearestNeighborDistanceMetric
        The distance metric used for measurement to track association.
    max_age : int
        Maximum number of missed misses before a track is deleted.
    n_init : int
        Number of frames that a track remains in initialization phase.
    kf : kalman_filter.KalmanFilter
        A Kalman filter to filter target trajectories in image space.
    tracks : List[Track]
        The list of active tracks at the current time step.

    """

    def __init__(self, metric, max_iou_distance=0.7, max_age=30, n_init=1, std_Q_w=1e-1, std_Q_wv=1e-3, std_R_w=5e-2):
        self.metric = metric
        self.max_iou_distance = max_iou_distance
        self.max_age = max_age
        self.n_init = n_init

        self.kf = kalman_filter.KalmanFilter(std_Q_w=std_Q_w, std_Q_wv=std_Q_wv, std_R_w=std_R_w)
        self.tracks = []
        self._next_id = 1

    def predict(self, frame_id=1):
        """Propagate track state distributions one time step forward.

        This function should be called once every time step, before `update`.
        """
        for track in self.tracks:
            track.predict(self.kf, frame_id)

    def update(self, detections, confidence_h):
        """Perform measurement update and track management.

        Parameters
        ----------
        detections : List[deep_sort.detection.Detection]
            A list of detections at the current time step.

        """
        # Run matching cascade.
        matches, unmatched_tracks, unmatched_detections = \
            self._match(detections)

        # Update track set.
        for track_idx, detection_idx in matches:
            self._next_id = self.tracks[track_idx].update(
                self.kf, detections[detection_idx], self._next_id)
        for track_idx in unmatched_tracks:  # old targets没有匹配上的没有进行位置更新
            self.tracks[track_idx].mark_missed()
        for detection_idx in unmatched_detections:  # 新检测到但是没有匹配上的，当置信度阈值符合要求时，认为是新目标
            if detections[detection_idx].confidence > confidence_h:
                self._initiate_track(detections[detection_idx])
        self.tracks = [t for t in self.tracks if not t.is_deleted()]

        # Update distance metric.
        active_targets = [t.track_id for t in self.tracks if t.is_confirmed()]
        features, targets = [], []
        for track in self.tracks:
            if not track.is_confirmed():  # 没有正常跟踪的不管
                continue
            features += track.features
            targets += [track.track_id for _ in track.features]
            track.features = []  # 正常跟踪的将feature置为[]
        # 更新最近邻分类器特征值
        self.metric.partial_fit(np.asarray(features), np.asarray(targets), active_targets)

    def _match(self, detections):

        def gated_metric(tracks, dets, track_indices, detection_indices):
            features = np.array([dets[i].feature for i in detection_indices])
            targets = np.array([tracks[i].track_id for i in track_indices])
            # 计算特征值的余弦距离
            cost_matrix = self.metric.distance(features, targets)
            cost_matrix = linear_assignment.gate_cost_matrix(self.kf, cost_matrix, tracks, dets, track_indices,
                                                             detection_indices)

            return cost_matrix

        # Split track set into confirmed and unconfirmed tracks.
        confirmed_tracks = [
            i for i, t in enumerate(self.tracks) if t.is_confirmed()]
        unconfirmed_tracks = [
            i for i, t in enumerate(self.tracks) if not t.is_confirmed()]

        # Associate confirmed tracks using appearance features. #特征匹配只针对confirmed_tracks
        matches_a, unmatched_tracks_a, unmatched_detections = \
            linear_assignment.matching_cascade(
                gated_metric, self.metric.matching_threshold, self.max_age,
                self.tracks, detections, confirmed_tracks)
        # matches_a [(i,j),(j2,j2)] 通过特征匹配上的序列組，(track_idx, detection_idx)

        # Associate remaining tracks together with unconfirmed tracks using IOU. # IOU匹配针对所有没有匹配上的
        iou_track_candidates = unconfirmed_tracks + [
            k for k in unmatched_tracks_a if
            self.tracks[k].time_since_update == 1]
        unmatched_tracks_a = [
            k for k in unmatched_tracks_a if
            self.tracks[k].time_since_update != 1]
        matches_b, unmatched_tracks_b, unmatched_detections = \
            linear_assignment.min_cost_matching(
                iou_matching.iou_cost, self.max_iou_distance, self.tracks,
                detections, iou_track_candidates, unmatched_detections)

        # matches_b [(i,j),(j2,j2)] 通过iou匹配上的序列，(track_idx, detection_idx) track的特征可能在两种地方。
        # matches_c = []
        # for i,matches_ in enumerate(matches_b):
        #     track_idx, detection_idx = matches_
        #     track_id = self.tracks[track_idx].track_id
        #     features = np.array([detections[detection_idx].feature])
        #     if track_idx in confirmed_tracks:
        #         track_feature_mat = self.metric.samples[track_id]
        #     else:
        #         track_feature_mat = np.array(self.tracks[track_idx].features)
        #     feature_dis = np.min(_cosine_distance(features,track_feature_mat))
        #
        #     if feature_dis>0.5:
        #         print('feature miss match ,iou match,feature distance:',feature_dis)
        #         # print('mean:',np.mean(_cosine_distance(features,track_feature_mat)))
        #         matches_c.append(matches_)
        #         unmatched_tracks_a.append(track_idx)
        #         unmatched_detections.append(detection_idx)
        # matches_b = list(set(matches_b)-set(matches_c))

        matches = matches_a + matches_b
        unmatched_tracks = list(set(unmatched_tracks_a + unmatched_tracks_b))
        return matches, unmatched_tracks, unmatched_detections

    def _initiate_track(self, detection):
        mean, covariance = self.kf.initiate(detection.to_xyah())
        new_track = Track(
            mean, covariance, self._next_id, self.n_init, self.max_age,
            detection.feature)
        new_track.predict(self.kf, 0)
        self._next_id = new_track.update(self.kf, detection, self._next_id)
        self.tracks.append(new_track)
        self._next_id += 1
