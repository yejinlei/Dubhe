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
import org.dubhe.tadl.domain.dto.*;
import org.dubhe.tadl.domain.entity.Experiment;
import org.dubhe.tadl.domain.entity.ExperimentStage;
import org.dubhe.tadl.domain.vo.BestAccuracyOutVO;
import org.dubhe.tadl.domain.vo.ExperimentFileVO;
import org.dubhe.tadl.domain.vo.ExperimentLogQueryVO;
import org.dubhe.tadl.domain.vo.ExperimentVO;
import org.dubhe.tadl.domain.vo.IntermediateAccuracyVO;
import org.dubhe.tadl.domain.vo.RunTimeOutVO;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 实验管理服务类
 * @date 2020-03-22
 */
public interface ExperimentService {

    /**
     * 查询单条实验记录
     *
     * @param experimentId   实验id
     * @return   实验对象
     */
    Experiment selectById(Long experimentId);

    /**
     * 根据查询条件获取单条实验记录
     * @param queryWrapper 查询条件
     * @return 实验对象
     */
    Experiment selectOne(LambdaQueryWrapper<Experiment>queryWrapper);

    /**
     * 实验列表查询
     *
     * @param experimentQueryDTO 实验查询dto
     * @return 实验列表
     */
    Map<String, Object> query(ExperimentQueryDTO experimentQueryDTO);

    /**
     * 根据 id 判断是否存在
     *
     * @param experimentId   实验id
     * @return   实验对象
     */
    boolean queryEmpty(Long experimentId);

    /**
     * 创建实验
     *
     * @param experimentCreateDTO 创建实验DTO
     */
    void create(ExperimentCreateDTO experimentCreateDTO);

    /**
     * 获取实验详情
     *
     * @param experimentId 实验ID
     * @return 实验详情VO
     */
    ExperimentVO info(Long experimentId);

    /**
     * 获取实验详情概览
     *
     * @param experimentId 实验ID
     * @return 实验详情VO
     */
    ExperimentVO getDetail(Long experimentId);

    /**
     * 编辑实验
     *
     * @param experimentUpdateDTO 实验编辑DTO
     */
    void update(ExperimentUpdateDTO experimentUpdateDTO);

    /**
     * 获取实验算法配置
     *
     * @param experimentId     算法名称
     * @return 实验所用算法所有阶段的配置
     */
    HashMap<String,Object> getConfiguration(Long experimentId);

    /**
     * 重启实验
     * @param experimentId 实验id
     */
    void restartExperiment(Long experimentId);

    /**
     * 启动实验
     * @param experimentId
     */
    void startExperiment(Long experimentId);

    /**
     * 启动实验
     * @param experiment 实验
     */
    void startExperiment(Experiment experiment);

    /**
     * 删除实验
     * @param experimentId 实验id
     */
    void deleteExperiment(Long experimentId);

    /**
     * 暂停实验
     * @param experimentId 实验id
     */
    void pauseExperiment(Long experimentId);


    /**
     * 获取search_space文件路径
     *
     * @param experimentId    算法名称
     * @return  search_space内容
     */
    ExperimentFileVO getSearchSpace(Long experimentId);

    /**
     * 获取best_selected_space文件路径
     *
     * @param experimentId    算法名称
     * @return  best_selected_space内容
     */
    ExperimentFileVO getBestSelectedSpace(Long experimentId);

    /**
     * 通过实验阶段组装推送消息
     * @param experimentId 实验id
     * @param experimentStage 实验阶段
     * @return ExperimentAndTrailDTO
     */
    ExperimentAndTrailDTO buildExperimentStageQueueMessage(Long experimentId, ExperimentStage experimentStage);

    /**
     * 获取当前阶段所有 trial 中间精度
     * @param experimentIntermediateAccuracyDTO
     * @return
     */
    IntermediateAccuracyVO getIntermediateAccuracy(ExperimentIntermediateAccuracyDTO experimentIntermediateAccuracyDTO);

    /**
     * 获取当前阶段所有 trial 最佳精度
     * @param experimentBestAccuracyDTO
     * @return TrialData
     */
    BestAccuracyOutVO getBestAccuracy(ExperimentBestAccuracyDTO experimentBestAccuracyDTO);

    /**
     * 获取当前阶段所有 trial 运行时间图
     * @param experimentRunTimeDTO
     * @return Trial
     */
    RunTimeOutVO getRunTime(ExperimentRunTimeDTO experimentRunTimeDTO);

    /**
     * 变更实验
     * @param wrapper 实验变更条件
     */
    void updateExperiment(LambdaUpdateWrapper<Experiment> wrapper);

    /**
     * 删除或恢复实验
     * @param experimentId 实验id
     * @param deleted 实验状态 true 删除 false 恢复
     */
    void updateExperimentDeletedById(Long experimentId, boolean deleted);

    /**
     * 查询实验运行日志
     *
     * @param experimentLogQueryDTO   experiment日志查询
     * @return ExperimentLogQueryVO   返回experiment日志查询
     **/
    ExperimentLogQueryVO queryExperimentLog(ExperimentLogQueryDTO experimentLogQueryDTO);

    /**
     * 实验运行失败
     * @param trialId 实验trial id
     * @param trialStatus trial状态
     * @param statusDetail 状态详情
     */
    void updateExperimentFailedByTrialId(Long trialId, Integer trialStatus, String statusDetail);
}
