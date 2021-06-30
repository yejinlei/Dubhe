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
import org.dubhe.datasetutil.domain.entity.DataFileAnnotation;

import java.util.List;

/**
 * @description nlp文件 服务实现类
 * @date 2021-01-07
 */
public interface DataFileAnnotationMapper extends BaseMapper<DataFileAnnotation> {

    /**
     * 批量保存nlp中间表
     *
     * @param dataFileAnnotations nlp集合
     */
    void saveDataFileAnnotation(@Param("dataFileAnnotations") List<DataFileAnnotation> dataFileAnnotations);

    /**
     * 删除数据集文件标注数据通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    @Delete("delete  from data_file_annotation where dataset_id = #{datasetId}")
    void delDataFileAnnotationById(long datasetId);
}
