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

package org.dubhe.service;

import org.dubhe.domain.dto.*;
import org.dubhe.domain.entity.PtTrainAlgorithm;

import java.util.Map;

/**
 * @description 训练算法 服务类
 * @date 2020-04-27
 */
public interface PtTrainAlgorithmService {

    /**
     * 查询数据分页
     *
     * @param criteria 分页参数条件
     * @return Map<String, Object>  map
     */
    Map<String, Object> queryAll(PtTrainAlgorithmQueryDTO criteria);

    /**
     * 新增算法
     *
     * @param resources 新增算法条件
     * @return PtTrainAlgorithmCreateVO  新建训练算法
     */
    Long create(PtTrainAlgorithmCreateDTO resources);

    /**
     * 修改算法
     *
     * @param resources 修改算法条件
     * @return PtTrainAlgorithmUpdateVO  修改训练算法
     */
    Long update(PtTrainAlgorithmUpdateDTO resources);

    /**
     * 删除算法
     *
     * @param ptTrainAlgorithmDeleteDTO 删除算法条件
     */
    void deleteAll(PtTrainAlgorithmDeleteDTO ptTrainAlgorithmDeleteDTO);

    /**
     * 查询当前用户的算法个数
     */
    Map<String, Object> getAlgorithmCount();

    /**
     * 模型优化上传算法
     *
     * @param ptModelAlgorithmCreateDTO 模型优化上传算法入参
     * @return PtTrainAlgorithm 新增算法信息
     */
    PtTrainAlgorithm modelOptimizationUploadAlgorithm(PtModelAlgorithmCreateDTO ptModelAlgorithmCreateDTO);

}
