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

package org.dubhe.algorithm.service;

import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmQueryDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUpdateDTO;
import org.dubhe.algorithm.domain.vo.PtTrainAlgorithmQueryVO;
import org.dubhe.biz.base.dto.ModelOptAlgorithmCreateDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectAllBatchIdDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectAllByIdDTO;
import org.dubhe.biz.base.dto.TrainAlgorithmSelectByIdDTO;
import org.dubhe.biz.base.vo.ModelOptAlgorithmQureyVO;
import org.dubhe.biz.base.vo.TrainAlgorithmQureyVO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import java.util.List;
import java.util.Map;

/**
 * @description 训练算法 服务类
 * @date 2020-04-27
 */
public interface PtTrainAlgorithmService {

    /**
     * 查询数据分页
     *
     * @param ptTrainAlgorithmQueryDTO 分页参数条件
     * @return Map<String, Object>  map
     */
    Map<String, Object> queryAll(PtTrainAlgorithmQueryDTO ptTrainAlgorithmQueryDTO);

    /**
     * 新增算法
     *
     * @param ptTrainAlgorithmCreateDTO 新增算法条件
     * @return PtTrainAlgorithmCreateVO  新建训练算法
     */
    List<Long> create(PtTrainAlgorithmCreateDTO ptTrainAlgorithmCreateDTO);

    /**
     * 修改算法
     *
     * @param ptTrainAlgorithmUpdateDTO 修改算法条件
     * @return PtTrainAlgorithmUpdateVO  修改训练算法
     */
    List<Long> update(PtTrainAlgorithmUpdateDTO ptTrainAlgorithmUpdateDTO);

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
     * 根据Id查询所有数据(包含已被软删除的数据)
     *
     * @param trainAlgorithmSelectAllByIdDTO 算法id
     * @return PtTrainAlgorithm 根据Id查询所有数据
     */
    TrainAlgorithmQureyVO selectAllById(TrainAlgorithmSelectAllByIdDTO trainAlgorithmSelectAllByIdDTO);

    /**
     * 根据Id查询
     *
     * @param trainAlgorithmSelectByIdDTO 算法id
     * @return PtTrainAlgorithm 根据Id查询
     */
    TrainAlgorithmQureyVO selectById(TrainAlgorithmSelectByIdDTO trainAlgorithmSelectByIdDTO);

    /**
     * 批量查询
     *
     * @param trainAlgorithmSelectAllBatchIdDTO 算法ids
     * @return List<PtTrainAlgorithm> 批量查询
     */
    List<TrainAlgorithmQureyVO> selectAllBatchIds(TrainAlgorithmSelectAllBatchIdDTO trainAlgorithmSelectAllBatchIdDTO);

    /**
     * 模型优化上传算法
     *
     * @param modelOptAlgorithmCreateDTO 模型优化上传算法入参
     * @return ModelOptAlgorithmQureyVO 新增算法信息
     */
    ModelOptAlgorithmQureyVO modelOptimizationUploadAlgorithm(ModelOptAlgorithmCreateDTO modelOptAlgorithmCreateDTO);

    /**
     * 算法删除文件还原
     * @param dto 还原实体
     */
    void algorithmRecycleFileRollback(RecycleCreateDTO dto);

    /**
     * 查询可推理算法
     * @return List<PtTrainAlgorithmQueryVO> 返回可推理算法集合
     */
    List<PtTrainAlgorithmQueryVO> getInferenceAlgorithm();
}
