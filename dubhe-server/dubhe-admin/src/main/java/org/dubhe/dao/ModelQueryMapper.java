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
package org.dubhe.dao;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.domain.dto.ModelQueryDTO;
import org.dubhe.domain.entity.ModelQuery;
import org.dubhe.domain.entity.ModelQueryBrance;

/**
 * @description model mapper
 * @date 2020-10-09
 */
public interface ModelQueryMapper extends BaseMapper<ModelQueryDTO> {
    /**
     * 根据modelId查询模型信息
     *
     * @param modelId 模型id
     * @return modelQuery返回查询的模型对象
     */
    @Select("select name,url from pt_model_info where id=#{modelId}")
    ModelQuery findModelNameById(@Param("modelId") Integer modelId);

    /**
     * 根据模型路径查询模型版本信息
     *
     * @param modelLoadPathDir 模型路径
     * @return ModelQueryBrance 模型版本信息
     */
    @Select("select version from pt_model_branch where url=#{modelLoadPathDir}")
    ModelQueryBrance findModelVersionByUrl(@Param("modelLoadPathDir") String modelLoadPathDir);
}
