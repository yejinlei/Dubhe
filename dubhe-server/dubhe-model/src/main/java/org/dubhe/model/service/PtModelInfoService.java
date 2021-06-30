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

package org.dubhe.model.service;

import org.dubhe.biz.base.dto.PtModelInfoConditionQueryDTO;
import org.dubhe.biz.base.dto.PtModelInfoQueryByIdDTO;
import org.dubhe.biz.base.vo.PtModelInfoQueryVO;
import org.dubhe.model.domain.dto.*;
import org.dubhe.model.domain.vo.PtModelInfoByResourceVO;
import org.dubhe.model.domain.vo.PtModelInfoCreateVO;
import org.dubhe.model.domain.vo.PtModelInfoDeleteVO;
import org.dubhe.model.domain.vo.PtModelInfoUpdateVO;

import java.util.List;
import java.util.Map;

/**
 * @description 模型管理
 * @date 2020-03-24
 */
public interface PtModelInfoService {

    /**
     * 查询数据分页
     *
     * @param ptModelInfoQueryDTO  模型管理查询参数
     * @return Map<String, Object> 模型管理分页对象
     */
    Map<String, Object> queryAll(PtModelInfoQueryDTO ptModelInfoQueryDTO);

    /**
     * 创建
     *
     * @param ptModelInfoCreateDTO 模型管理创建对象
     * @return PtModelInfoCreateVO 模型管理返回创建VO
     */
    PtModelInfoCreateVO create(PtModelInfoCreateDTO ptModelInfoCreateDTO);

    /**
     * 编辑
     *
     * @param ptModelInfoUpdateDTO 模型管理修改对象
     * @return PtModelInfoUpdateVO 模型管理返回更新VO
     */
    PtModelInfoUpdateVO update(PtModelInfoUpdateDTO ptModelInfoUpdateDTO);

    /**
     * 多选删除
     *
     * @param ptModelInfoDeleteDTO 模型管理删除对象
     * @return PtModelInfoDeleteVO 模型管理返回删除VO
     */
    PtModelInfoDeleteVO deleteAll(PtModelInfoDeleteDTO ptModelInfoDeleteDTO);

    /**
     * 根据模型来源查询模型信息
     *
     * @param ptModelInfoByResourceDTO   模型查询对象
     * @return PtModelInfoByResourceVO  模型返回查询VO
     */
    List<PtModelInfoByResourceVO> getModelByResource(PtModelInfoByResourceDTO ptModelInfoByResourceDTO);

    /**
     * 根据模型id查询模型详情
     *
     * @param ptModelInfoQueryByIdDTO 根据模型id查询模型详情查询参数
     * @return PtModelBranchQueryByIdVO 根据模型id查询模型详情返回结果
     */
    PtModelInfoQueryVO queryByModelId(PtModelInfoQueryByIdDTO ptModelInfoQueryByIdDTO);

    /**
     * 根据模型条件查询模型详情列表
     *
     * @param ptModelInfoConditionQueryDTO 查询条件
     * @return List<PtModelInfoQueryVO> 查询结果列表
     */
    List<PtModelInfoQueryVO> getConditionQuery(PtModelInfoConditionQueryDTO ptModelInfoConditionQueryDTO);

    /**
     * 模型优化上传模型
     *
     * @param ptModelOptimizationCreateDTO 模型优化上传模型入参
     * @return PtModelInfoByResourceVO  模型优化上传模型返回值
     */
    PtModelInfoByResourceVO modelOptimizationUploadModel(PtModelOptimizationCreateDTO ptModelOptimizationCreateDTO);


    /**
     * 将炼知模型打包
     *
     * @param ptModelInfoPackageDTO 打包参数
     * @return Boolean              打包结果 true 成功 false 失败
     */
    String packageAtlasModel(PtModelInfoPackageDTO ptModelInfoPackageDTO);

    /**
     * 查询能提供服务的模型
     * 现有：tensorflow和oneflow的都是savedmodel，pytorch的是pth
     * @return 能提供服务的模型实体集合
     */
    List<PtModelInfoQueryVO> getServingModel(ServingModelDTO servingModelDTO);
}
