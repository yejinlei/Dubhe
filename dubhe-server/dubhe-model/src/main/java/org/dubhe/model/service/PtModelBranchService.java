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

import org.dubhe.biz.base.dto.PtModelBranchConditionQueryDTO;
import org.dubhe.biz.base.dto.PtModelBranchQueryByIdDTO;
import org.dubhe.biz.base.vo.PtModelBranchQueryVO;
import org.dubhe.model.domain.dto.*;
import org.dubhe.model.domain.vo.PtModelBranchCreateVO;
import org.dubhe.model.domain.vo.PtModelBranchDeleteVO;
import org.dubhe.model.domain.vo.PtModelBranchUpdateVO;
import org.dubhe.model.domain.vo.PtModelConvertOnnxVO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import java.util.Map;

/**
 * @description 模型版本管理
 * @date 2020-03-24
 */
public interface PtModelBranchService {

    /**
     * 查询数据分页
     *
     * @param ptModelBranchQueryDTO 模型版本管理查询参数
     * @return Map<String, Object>  模型版本管理分页对象
     */
    Map<String, Object> queryAll(PtModelBranchQueryDTO ptModelBranchQueryDTO);

    /**
     * 创建
     *
     * @param ptModelBranchCreateDTO 模型版本管理创建对象
     * @return PtModelBranchCreateVO 模型版本管理返回创建VO
     */
    PtModelBranchCreateVO create(PtModelBranchCreateDTO ptModelBranchCreateDTO);

    /**
     * 编辑
     *
     * @param ptModelBranchUpdateDTO 模型版本管理修改对象
     * @return PtModelBranchUpdateVO 模型版本管理返回更新VO
     */
    PtModelBranchUpdateVO update(PtModelBranchUpdateDTO ptModelBranchUpdateDTO);

    /**
     * 多选删除
     *
     * @param ptModelBranchDeleteDTO 模型版本管理删除对象
     * @return PtModelBranchDeleteVO 模型版本管理返回删除VO
     */
    PtModelBranchDeleteVO deleteAll(PtModelBranchDeleteDTO ptModelBranchDeleteDTO);

    /**
     * 根据模型版本id查询模型版本详情
     *
     * @param ptModelBranchQueryByIdDTO 根据模型版本id查询模型版本详情查询参数
     * @return PtModelBranchQueryByIdVO 根据模型版本id查询模型版本详情返回结果
     */
    PtModelBranchQueryVO queryByBranchId(PtModelBranchQueryByIdDTO ptModelBranchQueryByIdDTO);


    /**
     * 条件查询模型版本详情
     *
     * @param ptModelBranchConditionQueryDTO 查询条件
     * @return PtModelBranchQueryVO 模型版本查询返回结果
     */
    PtModelBranchQueryVO getConditionQuery(PtModelBranchConditionQueryDTO ptModelBranchConditionQueryDTO);

    /**
     * 我的模型转预置模型
     * @param modelConvertPresetDTO 模型版本id请求体
     * @return
     */
    void convertPreset(ModelConvertPresetDTO modelConvertPresetDTO);

    /**
     * 模型删除文件还原
     * @param dto 还原实体
     */
    void modelRecycleFileRollback(RecycleCreateDTO dto);

    /**
     * TensorFlow SaveModel 模型转换为ONNX 模型
     *
     * @param ptModelConvertOnnxDTO 模型版本 id 请求体
     * @return
     */
    PtModelConvertOnnxVO convertToOnnx(PtModelConvertOnnxDTO ptModelConvertOnnxDTO);
}
