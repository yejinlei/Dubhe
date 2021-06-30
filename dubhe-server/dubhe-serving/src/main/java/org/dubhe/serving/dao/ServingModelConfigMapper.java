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
package org.dubhe.serving.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.serving.domain.entity.ServingModelConfig;

import java.util.List;
import java.util.Set;

/**
 * @description 模型部署配置
 * @date 2020-08-25
 */
public interface ServingModelConfigMapper extends BaseMapper<ServingModelConfig> {

    /**
     * 根据服务id，获取模型配置id集合
     *
     * @param servingId
     * @return
     */
    @Select("select id from serving_model_config where serving_id = #{servingId} and deleted = 0")
    Set<Long> getIdsByServingId(Long servingId);

    /**
     * 获取服务模型历史配置信息列表
     *
     * @param servingId
     * @return
     */
    List<ServingModelConfig> getRollbackList(Long servingId);

    /**
     * 获取服务另一个模型配置信息
     * @param servingId 服务id
     * @param modelConfigId 模型配置id
     * @return
     */
    @Select("select * from serving_model_config where serving_id = #{servingId} and id not in (#{modelConfigId}) and deleted = 0")
    ServingModelConfig selectAnother(@Param("servingId") Long servingId, @Param("modelConfigId") Long modelConfigId);

    /**
     * 还原回收数据
     *
     * @param id            serving model config id
     * @param deleteFlag    删除标识
     * @return int 数量
     */
    @Update("update serving_model_config set deleted = #{deleteFlag} where id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);
}
