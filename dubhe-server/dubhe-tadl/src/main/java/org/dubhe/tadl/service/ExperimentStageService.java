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
package org.dubhe.tadl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dubhe.tadl.domain.dto.MaxExecDurationUpdateDTO;
import org.dubhe.tadl.domain.dto.MaxTrialNumUpdateDTO;
import org.dubhe.tadl.domain.dto.TrialConcurrentNumUpdateDTO;
import org.dubhe.tadl.domain.dto.UpdateStageYamlDTO;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.domain.vo.ExperimentStageParamVO;
import org.dubhe.tadl.domain.vo.RuntimeParamVO;
import org.dubhe.tadl.domain.vo.StageOutlineVO;
import org.dubhe.tadl.domain.vo.TrialVO;

import java.util.List;

/**
 * @description 实验阶段服务类
 * @date 2020-03-22
 */
public interface ExperimentStageService {

    /**
     * 查询单条实验阶段记录
     *
     * @param experimentStageId   实验阶段id
     * @return   实验阶段对象
     */
    ExperimentStage selectById(Long experimentStageId);

    /**
     * 根据实验ID查询实验阶段状态列表
     *
     * @param experimentId      实验id
     * @return List<Integer>    实验阶段状态列表
     */
    List<Integer> getExperimentStateByStage(Long experimentId);

    /**
     * 根据实验id获取实验阶段列表
     * @param experimentId 实验id
     * @return 实验阶段列表
     */
    List<ExperimentStage> getExperimentStageListByExperimentId(Long experimentId);

    /**
     * 获取 experimentStage 列表
     *
     * @param wrapper 查询条件
     * @return 实验阶段列表
     */
    List<ExperimentStage> getExperimentStageList(LambdaQueryWrapper<ExperimentStage> wrapper);

    /**
     * 根据实验id获取实验阶段状态列表
     * @return 实验阶段列表
     */
    List<ExperimentStage> getStatusListSorted();
    /**
     * 创建实验阶段
     *
  * @param experimentStage 实验阶段
     */
    void insert(ExperimentStage experimentStage);

    /**
     * 根据阶段 id 更新实验阶段
     *
     * @param experimentStage 实验阶段
     */
    void updateExperimentStageById(ExperimentStage experimentStage);

    /**
     * 查询阶段概览
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段排序
     * @return  阶段概览
     */
    StageOutlineVO query(Long experimentId, Integer stageOrder);

    /**
     * 查询实验阶段参数
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return 实验运行参数VO
     */
    ExperimentStageParamVO queryStageParam(Long experimentId, Integer stageOrder);

    /**
     * 查询实验阶段运行参数
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return 实验运行参数VO
     */
    RuntimeParamVO queryRuntimeParam(Long experimentId, Integer stageOrder);

    List<TrialVO> queryTrialRep(Long experimentId, Integer stageOrder);

    /**
     * 查询实验阶段运行参数
     *
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return 实验阶段对象
     */
    ExperimentStage selectOne(Long experimentId, Integer stageOrder);

    /**
     * 获取实验当前阶段 yaml 配置
     * @param experimentId 实验ID
     * @param stageOrder   阶段ID
     * @return yaml
     */
    String getConfiguration(Long experimentId, Integer stageOrder);

    /**
     * 修改实验阶段yaml
     *
     * @param updateStageYamlDTO 修改yaml DTO
     */
    void updateConfiguration(UpdateStageYamlDTO updateStageYamlDTO);

    /**
     * 修改最大运行时间
     *
     * 最长持续时间：
     *      1. 实验最长持续时间不可少于当前已运行时间且运行时间单位为系统有效单位
     *
     * @param maxExecDurationUpdateDTO     最大执行时间
     */
    void updateMaxExecDuration(MaxExecDurationUpdateDTO maxExecDurationUpdateDTO);

    /**
     * 修改最大 trial 数
     *
     * 最大 trial 数
     *      1. 实验最大trial数不可少于已完成数量（判断标准？）
     *
     * @param maxTrialNumUpdateDTO  最大trial数
     */
    void updateMaxTrialNum(MaxTrialNumUpdateDTO maxTrialNumUpdateDTO);

    /**
     * 修改 trial 并发数
     *
     * trial 并发数
     *      1. trial 并发数不能大于当前 trial 总数 ，如果是单一的 trial 则不能修改
     *
     * @param trialConcurrentNumUpdateDTO    最大并发数
     */
    void updateTrialConcurrentNum(TrialConcurrentNumUpdateDTO trialConcurrentNumUpdateDTO);

    /**
     * 更新实验阶段
     * @param wrapper 更新条件
     * @return 更新数量
     */
    Integer updateExperimentStage(LambdaUpdateWrapper<ExperimentStage> wrapper);

    /**
     * 批量生成实验阶段
     * @param experimentStageList 实验阶段集合
     * @return 批量生成数量
     */
    Integer insertExperimentStageList(List<ExperimentStage> experimentStageList);

    /**
     * 计算出实验阶段结束时间戳，存入redis的zset中
     * @param experimentId 实验ID
     * @param experimentStageId   实验阶段id
     */
    void saveExpiredTimeToRedis(Long experimentId, Long experimentStageId);

    /**
     * 将实验阶段的结束时间设置为2099年，表示暂停
     * @param experimentId 实验ID
     * @param experimentStageId   实验阶段id
     */
    void pauseExpiredTimeToRedis(Long experimentId, Long experimentStageId);

    /**
     * 将试验阶段的结束时间从zset中移除
     * @param experimentId 实验ID
     * @param experimentStageId   实验阶段id
     */
    void removeExpiredTimeToRedis(Long experimentId, Long experimentStageId);
}
