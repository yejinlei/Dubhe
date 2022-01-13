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
package org.dubhe.tadl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.tadl.domain.entity.AlgorithmStage;

/**
 * @description 算法阶段管理服务Mapper
 * @date 2021-03-22
 */
public interface AlgorithmStageMapper extends BaseMapper<AlgorithmStage>{
    /**
     * 变更算法阶段删除标识
     * @param versionId 算法版本id
     * @param deleted 删除标识
     * @return
     */
    @Update("update tadl_algorithm_stage set deleted=#{deleted} where algorithm_version_id = #{versionId}")
    int updateStageStatusByVersionId(@Param("versionId") Long versionId, @Param("deleted") Boolean deleted);

    /**
     * 通过id查询算法阶段
     * @param id 算法阶段id
     * @return AlgorithmStage
     */
    @Select("select * from tadl_algorithm_stage where id = #{id}")
    AlgorithmStage getOneById(@Param("id") Long id);
}
