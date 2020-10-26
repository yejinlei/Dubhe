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

import org.dubhe.datasetutil.domain.entity.Dataset;

/**
 * @description 数据集服务
 * @date 2020-9-17
 */
public interface DatasetService {
    /**
     * 根据数据集Id查询创建人Id
     *
     * @param datasetId 数据集Id
     * @return Dataset 数据集
     */
    Dataset findCreateUserIdById(Long datasetId);

    /**
     * 根据ID查询数据集
     *
     * @param datasetId 数据集Id
     * @return Dataset 数据集
     */
    Dataset findDatasetById(Long datasetId);

    /**
     * 更新数据集状态
     *
     * @param dataset 数据集
     * @return int 数量
     */
    int updateDatasetStatus(Dataset dataset);

    /**
     * 查询数据集标签数量
     *
     * @param datasetId 数据集ID
     * @return int 数量
     */
    int findDataLabelById(Long datasetId);

    /**
     * 查询数据集文件数量
     *
     * @param datasetId 数据集ID
     * @return int 数量
     */
    int findDataFileById(Long datasetId);
}
