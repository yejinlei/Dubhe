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

package org.dubhe.data.dao;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.data.domain.entity.Label;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.dubhe.annotation.DataPermission;

import java.util.List;
import java.util.Set;

/**
 * @description 数据集标签管理 Mapper 接口
 * @date 2020-04-10
 */
@DataPermission(ignores = {"insert", "listLabelByDatasetId", "getDatasetLabelTypes", "selectListByType", "batchListByIds"})
public interface LabelMapper extends BaseMapper<Label> {

    /**
     * 查询数据集下所有标签
     *
     * @param datasetId 数据集ID
     * @return List<Label> 数据集下所有标签
     */
    @Select("select dl.* from data_label dl left join data_dataset_label ddl on dl.id = ddl.label_id where ddl.dataset_id = #{datasetId}")
    List<Label> listLabelByDatasetId(@Param("datasetId") Long datasetId);

    /**
     * 获取数据集下所有标签类型
     *
     * @param datasetId 数据集ID
     * @return List<Integer> 数据集下所有标签类型
     */
    @Select("select distinct dl.type from data_label dl left join data_dataset_label ddl on dl.id = ddl.label_id where ddl.dataset_id = #{datasetId}")
    List<Integer> getDatasetLabelTypes(@Param("datasetId") Long datasetId);

    /**
     * 根据标签类型获取标签
     *
     * @param type 标签类型
     * @return List<Label> 标签类型获取标签
     */
    @Select("select * from data_label where `type` = #{type}")
    List<Label> selectListByType(@Param("type") Integer type);

    /**
     * 搜索
     *
     * @param datasetId 数据集ID
     * @return List<Label> 搜索标签信息
     */
    List<Label> batchListByIds(@Param("datasetId") Long datasetId);

}
