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
import org.dubhe.tadl.domain.entity.Experiment;

/**
 * @description 实验管理服务Mapper
 * @date 2021-03-22
 */
public interface ExperimentMapper extends BaseMapper<Experiment>{
    /**
     * 根据实验id删除实验，实验阶段及trial实验数据
     * @param id 实验id
     * @param deleted 删除状态
     * @return
     */
    int updateExperimentDeletedById(@Param("id") Long id,@Param("deleted") Boolean deleted);

    /**
     * 根据trial实验id变更实验为运行失败
     * @param trialId trial实验id
     * @param trialStatus trial 状态
     * @param stageStatus 实验阶段状态
     * @param experimentStatus 实验状态
     * @param statusDetail 状态详情
     */
    void updateExperimentFailedByTrialId(@Param("trialId") Long trialId,@Param("trialStatus")Integer trialStatus,@Param("stageStatus") Integer stageStatus,@Param("experimentStatus") Integer experimentStatus,@Param("statusDetail")String statusDetail);
}
