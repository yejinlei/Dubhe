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
import org.dubhe.tadl.domain.entity.ExperimentStage;

import java.util.List;

/**
 * @description 实验阶段管理服务Mapper
 * @date 2021-03-22
 */
public interface ExperimentStageMapper extends BaseMapper<ExperimentStage>{

    /**
     * 根据实验ID查询实验阶段状态列表
     *
     * @param experimentId      实验id
     * @return List<Integer>    实验阶段状态列表
     */
    List<Integer> getExperimentStateByStage(@Param("experimentId") Long experimentId);

    /**
     * 更改实验阶段状态
     * @param id 实验阶段 id
     * @param status 实验阶段状态
     */
    void updateExperimentStageStatus(@Param("id") Long id,@Param("status") Integer status);

    /**
     * 根据实验Id和实验阶段id查找
     * @param experimentId 实验 id
     * @param experimentStageId 实验阶段 id
     * @return
     */
    ExperimentStage getExperimentStateByExperimentIdAndStageId(@Param("experimentId") Long experimentId,@Param("experimentStageId") Long experimentStageId);

    /**
     * 批量插入实验阶段
     * @param experimentStageList 实验阶段集合
     * @return 批量插入数量
     */
    Integer insertExperimentStageList(@Param("experimentStageList") List<ExperimentStage> experimentStageList);
}
