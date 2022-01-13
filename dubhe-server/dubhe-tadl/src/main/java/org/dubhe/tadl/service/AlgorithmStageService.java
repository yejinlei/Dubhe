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
import org.dubhe.tadl.domain.entity.AlgorithmStage;

import java.util.List;

/**
 * @description 算法阶段服务类
 * @date 2020-03-22
 */
public interface AlgorithmStageService {

    /**
     * 创建算法写算法阶段
     *
     * @param algorithmStage 算法阶段
     */
    void insertStages(List<AlgorithmStage> algorithmStage);

    /**
     * 根据条件查询阶段列表
     *
     * @param wrapper 条件
     * @return 阶段列表
     */
    List<AlgorithmStage> selectList(LambdaQueryWrapper<AlgorithmStage> wrapper);

    /**
     * 根据ID查询未删除的实验阶段对象
     *
     * @param stageId 实验阶段ID
     * @return 实验阶段对象
     */
    AlgorithmStage selectOneById(Long stageId);

    /**
     * 更新实验阶段
     *
     * @param algorithmStage 实验阶段
     */
    void updateAlgorithmStage(List<AlgorithmStage> algorithmStage);
    /**
     * 更新实验阶段
     * @param updateWrapper 更新条件
     */
    void updateAlgorithmStage(LambdaUpdateWrapper<AlgorithmStage> updateWrapper);

    /**
     * 更新实验阶段状态
     * @param versionId 算法版本id
     * @param deleted 删除标识
     * @return 变更数量
     */
    int updateStageStatusByVersionId(Long versionId, Boolean deleted);

    /**
     * 删除指定算法下所有数据
     * @param algorithmId 算法id
     * @return 变更数量
     */
    int deleteByAlgorithmId(Long algorithmId);

    /**
     * 根据算法阶段id获取已删除或者未删除算法阶段
     * @param algorithmStageId 算法阶段id
     * @return 算法阶段
     */
    AlgorithmStage getOneById(Long algorithmStageId);
}
