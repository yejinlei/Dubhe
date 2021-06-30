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

package org.dubhe.optimize.service;

import org.dubhe.optimize.domain.dto.ModelOptDatasetCreateDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskCreateDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskDeleteDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskQueryDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskSubmitDTO;
import org.dubhe.optimize.domain.dto.ModelOptTaskUpdateDTO;
import org.dubhe.optimize.domain.vo.ModelOptAlgorithmQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptCreateVO;
import org.dubhe.optimize.domain.vo.ModelOptDatasetQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptDatasetVO;
import org.dubhe.optimize.domain.vo.ModelOptModelQueryVO;
import org.dubhe.optimize.domain.vo.ModelOptUpdateVO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import java.util.List;
import java.util.Map;

/**
 * @description 模型优化任务
 * @date 2020-05-22
 */
public interface ModelOptTaskService {

    /**
     * 查询数据分页
     *
     * @param modelOptTaskQueryDTO 模型优化任务查询参数
     * @return Map<String, Object> 模型优化任务分页对象
     */
    Map<String, Object> queryAll(ModelOptTaskQueryDTO modelOptTaskQueryDTO);

    /**
     * 创建模型优化任务
     * @param modelOptTaskCreateDTO 模型优化任务创建对象
     * @return 返回创建成功信息
     */
    ModelOptCreateVO create(ModelOptTaskCreateDTO modelOptTaskCreateDTO);

    /**
     * 提交模型优化任务，创建任务实例
     *
     * @param submitDTO 任务提交参数
     */
    void submit(ModelOptTaskSubmitDTO submitDTO);

    /**
     * 修改模型优化任务
     * @param modelOptTaskUpdateDTO 模型优化任务修改对象
     * @return 返回修改成功信息
     */
    ModelOptUpdateVO update(ModelOptTaskUpdateDTO modelOptTaskUpdateDTO);

    /**
     * 删除模型优化任务
     *
     * @param modelOptTaskDeleteDTO 模型优化任务删除参数
     */
    void delete(ModelOptTaskDeleteDTO modelOptTaskDeleteDTO);

    /**
     * 获取内置模型
     *
     * @param type      算法类型
     * @param dataset   数据集
     * @param algorithm 训练算法
     * @return List<String> 内置模型列表
     */
    List<ModelOptModelQueryVO> getBuiltInModel(Integer type, String dataset, String algorithm);

    /**
     * 获取优化算法
     *
     * @param type      算法类型
     * @param model     模型
     * @param dataset   数据集
     * @return List<ModelOptAlgorithmVO> 获取模型优化算法列表
     */
    List<ModelOptAlgorithmQueryVO> getAlgorithm(Integer type, String model, String dataset);

    /**
     * 获取模型优化数据集
     *
     * @param type      算法类型
     * @param model     模型
     * @param algorithm 训练算法
     * @return List<ModelOptDatasetQueryVO> 模型优化数据集列表
     */
    List<ModelOptDatasetQueryVO> getDataset(Integer type, String model, String algorithm);

    /**
     * 获取我的模型优化数据集
     *
     * @return List<ModelOptDatasetVO> 我的模型优化数据集列表
     */
    List<ModelOptDatasetVO> getMyDataset();

    /**
     * 创建我的模型优化数据集
     *
     * @param modelOptDatasetCreateDTO 数据集创建参数
     * @return ModelOptDatasetVO       模型优化模块我的数据集查询
     */
    ModelOptDatasetVO createMyDataset(ModelOptDatasetCreateDTO modelOptDatasetCreateDTO);

    /**
     * 模型优化任务数据还原
     *
     * @param dto 还原DTO对象
     */
    void recycleRollback(RecycleCreateDTO dto);
}
