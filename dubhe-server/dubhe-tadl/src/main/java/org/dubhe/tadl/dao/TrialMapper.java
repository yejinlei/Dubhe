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
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.domain.entity.TrialData;

import java.util.List;

/**
 * @description 试验管理服务Mapper
 * @date 2021-03-22
 */
public interface TrialMapper extends BaseMapper<Trial>{

    /**
     * 根据实验阶段ID查询trial状态列表
     *
     * @param experimentStageId      实验阶段id
     * @return List<Integer>         trial 状态set
     */
    List<Integer> getExperimentStageStateByTrial(@Param("experimentStageId") Long experimentStageId);


    /**
     * 批量写入trial
     *
     * @param trials trial 列表
     */
    void saveList(@Param("trials")List<Trial> trials);

    /**
     * 获取当前阶段最佳的精度
     *
     * @param experimentId 实验ID
     * @param stageId      阶段ID
     * @return 当前阶段最佳精度
     */
    double getBestData(@Param("experimentId") Long experimentId,@Param("stageId") Long stageId);

    /**
     * 更新trial实验状态
     * @param id trial ID
     * @param status trial 状态
     */
    void updateTrialStatus(@Param("id") Long id ,@Param("status") Integer status);

    /**
     * 根据 实验id和阶段id 查询TrialData表
     * @param experimentId
     * @param stageId
     * @return TrialData
     */
    List<TrialData> queryTrialDataById(@Param("experimentId") Long experimentId,@Param("stageId") Long stageId);

    /**
     * 根据 实验id和阶段id 查询Trial表
     * @param experimentId
     * @param stageId
     * @return
     */
    List<Trial> queryTrialById(@Param("experimentId") Long experimentId,@Param("stageId") Long stageId,@Param("trialIds") List<Long> trialIds,@Param("statusList") List<Integer> statusList);

    /**
     * 根据id变更trial 为失败
     * @param trialId trial id
     * @param trialStatus trial 实验状态
     * @param stageStatus 实验阶段状态
     * @param experimentStatus 实验状态
     * @param statusDetail 状态详情
     */
    void updateTrialFailed(@Param("id") Long trialId,@Param("trialStatus")Integer trialStatus,@Param("stageStatus") Integer stageStatus,@Param("experimentStatus") Integer experimentStatus,@Param("statusDetail")String statusDetail);

    /**
     * 获取成功的trial数量
     * @param experimentId
     * @param stageId
     * @return 成功的trial数量
     */
    Integer getTrialCountOfStatus(Long experimentId,Long stageId,Integer status);
}
