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

import org.dubhe.data.domain.entity.DatasetLabel;
import org.dubhe.data.domain.entity.Label;

import java.util.List;

/**
 * @description 数据集标签信息服务
 * @date 2020-04-17
 */
public interface DatasetLabelService {

    void saveList(List<DatasetLabel> datasetLabels);

    /**
     * 标签列表
     *
     * @param datasetId 数据集id
     * @return List<DatasetLabel> 标签列表
     */
    List<DatasetLabel> list(Long datasetId);

    /**
     * 删除标签
     *
     * @param datasetId 数据集id
     * @return int 执行次数
     */
    int del(Long datasetId);

    /**
     * 过滤存在的标签
     *
     * @param rels 当前数据集标签
     * @return DatasetLabel 过滤后的标签
     */
    List<DatasetLabel> filterExist(List<DatasetLabel> rels);

    /**
     * 获取数据集下所有标签
     *
     * @param datasetId 数据集id
     * @return List<label> 数据集下所有标签
     */
    List<Label> listLabelByDatasetId(Long datasetId);

}
