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

package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.data.domain.entity.DatasetLabel;

import java.util.List;

/**
 * @description 数据集标签 Mapper 接口
 * @date 2020-04-17
 */
public interface DatasetLabelMapper extends BaseMapper<DatasetLabel> {

    @Update("update data_dataset_label set deleted = #{deleteFlag} where dataset_id = #{datasetId}")
    void updateStatusByDatasetId(@Param("datasetId") Long datasetId, @Param("deleteFlag") Boolean deleteFlag);


    /**
     * 根据数据集ID删除数据标签数据
     *
     * @param datasetId     数据集ID
     * @return int 成功删除条数
     */
    @Delete("delete from data_dataset_label where dataset_id = #{datasetId}  ")
    int deleteByDatasetId(@Param("datasetId") Long datasetId);

    /**
     * 批量新增数据集标签数据
     *
     * @param datasetLabels 数据集标签列表
     */
    void insertBatch(List<DatasetLabel> datasetLabels);
}
