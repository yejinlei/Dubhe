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
package org.dubhe.datasetutil.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.dubhe.datasetutil.domain.entity.DataLabel;

import java.util.List;

/**
 * @description 数据集标签
 * @date 2020-10-14
 */
public interface DataLabelMapper extends BaseMapper<DataLabel> {
    /**
     * 批量保存数据集标签
     *
     * @param listDataLabel 标签数据
     */
    void saveBatchDataLabel(@Param("listDataLabel") List<DataLabel> listDataLabel);



    /**
     * 根据预置标签组获取预置标签
     *
     * @param groupIds 预置标签组IDS
     * @return  预置标签 key: 预置标签名称 value:预置标签ID
     */
    List<DataLabel> getPresetLabelList(@Param("groupIds") List<Long> groupIds);

    /**
     * 删除标签
     *
     * @param datasetId
     */
    @Delete("delete from data_label where id  IN (\n" +
            "   select * from ( select label_id from data_dataset_label where dataset_id = #{datasetId}) t\n" +
            ")")
    void deleteLabelByDatasetId(@Param("datasetId") long datasetId);
}
