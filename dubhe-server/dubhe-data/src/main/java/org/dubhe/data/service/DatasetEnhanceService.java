/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
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

import org.dubhe.data.domain.dto.DatasetEnhanceFinishDTO;
import org.dubhe.data.domain.dto.DatasetEnhanceRequestDTO;
import org.dubhe.data.domain.entity.DatasetVersionFile;
import org.dubhe.data.domain.entity.Task;

import java.util.List;

/**
 * @description 数据集增强服务
 * @date 2020-06-28
 */
public interface DatasetEnhanceService {

    /**
     * 提交任务
     *
     * @param datasetVersionFiles      数据集版本文件列表
     * @param task                     任务
     * @param datasetEnhanceRequestDTO 提交任务参数
     */
    void commitEnhanceTask(List<DatasetVersionFile> datasetVersionFiles, Task task, DatasetEnhanceRequestDTO datasetEnhanceRequestDTO);

    /**
     * 增强任务完成
     *
     * @param datasetEnhanceFinishDTO 数据集增强完成详情
     */
    void enhanceFinish(DatasetEnhanceFinishDTO datasetEnhanceFinishDTO);

}
