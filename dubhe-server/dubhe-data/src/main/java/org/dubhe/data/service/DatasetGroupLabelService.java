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

import org.dubhe.data.domain.entity.DatasetGroupLabel;

import java.util.List;

/**
 * @description 标签组标签中间表服务
 * @date 2020-10-16
 */
public interface DatasetGroupLabelService {


    /**
     * 新增标签组标签中间表信息
     *
     * @param datasetGroupLabel 新增标签组标签中间表实体
     * @return  新增新增标签组标签中间表结果
     */
    int insert(DatasetGroupLabel datasetGroupLabel);

    /**
     * 根据标签组id查询标签关联关系列表
     *
     * @param groupId 标签组id
     * @return  标签标签组关联关系列表
     */
    List<DatasetGroupLabel> listByGroupId(Long groupId);

    /**
     * 根据标签id查询标签关联关系列表
     *
     * @param labelId 标签id
     * @return  标签标签组关联关系列表
     */
    List<DatasetGroupLabel> listByLabelId(Long labelId);

    /**
     * 根据标签组ID删除标签和标签组的关联关系
     *
     * @param groupId 标签组ID
     */
    void deleteById(Long groupId);
}
