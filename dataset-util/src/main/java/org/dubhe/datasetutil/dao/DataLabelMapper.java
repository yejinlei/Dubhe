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
}
