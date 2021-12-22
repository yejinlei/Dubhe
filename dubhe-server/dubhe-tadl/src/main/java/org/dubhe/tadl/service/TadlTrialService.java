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
import org.dubhe.k8s.domain.vo.PodVO;
import org.dubhe.tadl.domain.dto.TrialDTO;
import org.dubhe.tadl.domain.entity.Trial;

import java.util.List;
import java.util.Map;

/**
 * @description Tadl 服务类
 * @date 2020-12-28
 */
public interface TadlTrialService {

    /**
     * trial列表查询
     *
     * @param trialDTO trial查询dto
     * @return trial列表
     */
    Map<String, Object> listVO(TrialDTO trialDTO);

    /**
     * 根据实验阶段ID查询trial状态列表
     *
     * @param experimentStageId      实验阶段id
     * @return List<Integer>         trial 状态set
     */
    List<Integer> getExperimentStageStateByTrial(Long experimentStageId);

    /**
     * 批量写入trial
     *
     * @param trials trial 列表
     */
    void insertList(List<Trial> trials);

    /**
     * 获取 trial 列表
     *
     * @param wrapper 查询条件
     * @return
     */
    List<Trial> getTrialList(LambdaQueryWrapper<Trial> wrapper);
    /**
     * 删除 trial
     *
     * @param wrapper 删除实验 wrapper
     */
    void delete(LambdaQueryWrapper<Trial> wrapper);


    /**
     * 查询 trial 数量
     * @param wrapper 查询条件
     * @return trial 数量
     */
    Integer selectCount(LambdaQueryWrapper<Trial> wrapper);

    /**
     * 获取当前阶段最佳的精度
     *
     * @param experimentId 实验ID
     * @param stageId      阶段ID
     * @return 当前阶段最佳精度
     */
    double getBestData(Long experimentId, Long stageId);

    /**
     * 查询一个 trial
     *
     * @param id trial ID
     * @return trial
     */
    Trial selectOne(Long id);

    /**
     * trial数据更新
     * @param wrapper 变更条件
     * @return trial 数量
     */
    Integer updateTrial(LambdaUpdateWrapper<Trial> wrapper);

    /**
     * 变更trial状态为运行失败
     * @param trialId trial id
     */
    void updateTrialFailed(Long trialId,String statusDetail);

    /**
     * 获取trial下pod信息
     *
     * @param id 服务配置id
     * @return 服务配下的pod信息
     */
     List<PodVO> getPods(Long id);


}
