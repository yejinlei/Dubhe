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
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.annotation.DataPermission;
import org.dubhe.data.domain.entity.LabelGroup;

/**
 * @description 标签组管理 Mapper 接口
 * @date 2020-09-22
 */
@DataPermission(ignoresMethod = {"insert","getLabelGroupByDataId","selectById"})
public interface LabelGroupMapper extends BaseMapper<LabelGroup> {


    /**
     * 根据数据集ID查询标签组信息
     *
     * @param datasetId 数据集id
     * @return LabelGroup 标签组实体
     */
    @Select("select dlg.id , dlg.label_group_type from data_label_group dlg left join  data_dataset dd on dd.label_group_id = dlg.id\n" +
            "where dd.id = #{datasetId}")
    LabelGroup getLabelGroupByDataId(@Param("datasetId") Long datasetId);

}
