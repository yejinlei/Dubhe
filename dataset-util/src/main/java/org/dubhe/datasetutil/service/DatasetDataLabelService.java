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
package org.dubhe.datasetutil.service;

import org.dubhe.datasetutil.domain.entity.DatasetDataLabel;

import java.util.List;

/**
 * @description 数据集与标签关系服务接口
 * @date 2020-10-14
 */
public interface DatasetDataLabelService {
    /**
     * 批量保存数据集与标签关系
     *
     * @param listDatasetDataLabel 数据集标签集合
     */
    void saveBatchDatasetDataLabel(List<DatasetDataLabel> listDatasetDataLabel);

    /**
     * 删除数据集标签关系通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    void deleteDatasetLabelByDatasetId(long datasetId);
}
