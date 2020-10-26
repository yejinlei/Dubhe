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
import org.apache.ibatis.annotations.Param;
import org.dubhe.datasetutil.domain.entity.DataGroupLabel;

import java.util.List;

/**
 * @description 标签组与标签的关系MAPPER
 * @date 2020-10-21
 */
public interface DataGroupLabelMapper extends BaseMapper<DataGroupLabel> {
    /**
     * 插入标签组与标签的关数据
     *
     * @param listDataGroupLabel 标签组与标签数据集合
     */
    void saveDataGroupLabel(@Param("list") List<DataGroupLabel> listDataGroupLabel);
}
