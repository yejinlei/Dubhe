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
import org.dubhe.datasetutil.domain.dto.DataVersionFile;

import java.util.List;

/**
 * @description 数据集文件中间表 Mapper接口
 * @date 2020-9-17
 */
public interface DataVersionFileMapper extends BaseMapper<DataVersionFile> {

    /**
     * 插入数据集文件中间表数据
     *
     * @param listDataVersionFile 数据集文件中间表数据集合
     */
    void saveBatchDataFileVersion(@Param("listDataVersionFile") List<DataVersionFile> listDataVersionFile);
}
