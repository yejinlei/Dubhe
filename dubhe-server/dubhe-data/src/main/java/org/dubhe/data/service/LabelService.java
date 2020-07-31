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

import org.dubhe.data.domain.entity.Label;

import java.util.List;

/**
 * @description 标签服务
 * @date 2020-04-10
 */
public interface LabelService {

    /**
     * 根据数据ID获取包含标签列表
     *
     * @param datasetId 数据ID
     * @return List<Label> 根据数据ID获取包含标签列表
     */
    List<Label> list(Long datasetId);

    /**
     * 获取所有自动标注标签
     *
     * @return List<Label> 获取所有自动标注标签
     */
    List<Label> listSupportAuto();

    /**
     * 保存标签
     *
     * @param label     标签
     * @param datasetId 数据集id
     */
    Long save(Label label, Long datasetId);

    /**
     * 保存标签
     *
     * @param labels    标签
     * @param datasetId 数据集id
     */
    void save(List<Label> labels, Long datasetId);

    /**
     * 获取指定类型下所有标签
     *
     * @param type 标签类型
     * @return List<Label> 指定类型下所有标签
     */
    List<Label> listByType(Integer type);

    /**
     * 删除数据集标签
     *
     * @param id 数据集id
     * @return int 执行次数
     */
    int delDataset(Long id);

    /**
     * 获取数据集下所有标签类型
     *
     * @param datasetId 数据集id
     * @return List<Integer> 数据集下所有标签类型
     */
    List<Integer> getDatasetLabelTypes(Long datasetId);

}
