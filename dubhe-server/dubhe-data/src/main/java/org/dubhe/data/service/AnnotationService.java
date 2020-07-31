/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */

package org.dubhe.data.service;

import org.dubhe.data.domain.bo.TaskSplitBO;
import org.dubhe.data.domain.dto.AnnotationDeleteDTO;
import org.dubhe.data.domain.dto.AnnotationInfoCreateDTO;
import org.dubhe.data.domain.dto.AutoTrackCreateDTO;
import org.dubhe.data.domain.dto.BatchAnnotationInfoCreateDTO;

import java.util.Map;

/**
 * @description 标注信息服务类
 * @date 2020-07-16
 */
public interface AnnotationService {

    /**
     * 标注保存(分类批量)
     *
     * @param batchAnnotationInfoCreateDTO 标注信息
     * @return int 标注成功数量
     */
    int save(BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO);

    /**
     * 标注保存实现
     *
     * @param annotationInfoCreateDTO 标注信息
     * @return int 标注成功数量
     */
    int save(AnnotationInfoCreateDTO annotationInfoCreateDTO);


    /**
     * 保存标注(单个)
     *
     * @param fileId                  文件id
     * @param annotationInfoCreateDTO 保存标注参数
     * @return int 标注成功数量
     */
    int save(Long fileId, AnnotationInfoCreateDTO annotationInfoCreateDTO);

    /**
     * 标注完成
     *
     * @param annotationInfoCreateDTO 标注信息
     * @param fileId                      文件id
     * @return int 标注完成的数量
     */
    int finishManual(Long fileId, AnnotationInfoCreateDTO annotationInfoCreateDTO);

    /**
     * 标注清除
     *
     * @param annotationDeleteDTO 标注清除条件
     * @return boolean 清除标注是否成功
     */
    void delete(AnnotationDeleteDTO annotationDeleteDTO);

    /**
     * 完成自动标注
     *
     * @param taskId 子任务id
     * @param batchAnnotationInfoCreateDTO
     * @return boolean 标注任务完成与否 失败则抛出异常(200)
     */
    boolean finishAuto(String taskId, BatchAnnotationInfoCreateDTO batchAnnotationInfoCreateDTO);

    /**
     * 获取任务map
     *
     * @return Map<String, TaskSplitBO> 当前正在标注中的任务(已经发送给算法的)
     */
    Map<String, TaskSplitBO> getTaskPool();

    /**
     * 完成目标跟踪
     *
     * @param datasetId          数据集id
     * @param autoTrackCreateDTO 目标跟踪条件
     * @return boolean 目标跟踪是否成功
     */
    void finishAutoTrack(Long datasetId, AutoTrackCreateDTO autoTrackCreateDTO);

}
