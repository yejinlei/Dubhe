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
package org.dubhe.dcm.service;


import com.alibaba.fastjson.JSONObject;
import org.dubhe.biz.base.enums.OperationTypeEnum;
import org.dubhe.dcm.domain.dto.*;
import org.dubhe.dcm.domain.entity.DataMedicine;
import org.dubhe.dcm.domain.vo.DataMedicineCompleteAnnotationVO;
import org.dubhe.dcm.domain.vo.DataMedicineVO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import java.util.Map;

/**
 * @description 医学数据集服务类
 * @date 2020-11-11
 */
public interface DataMedicineService {


    /**
     * 根据医学数据集ID获取数据集
     *
     * @param medicineId    医学数据集ID
     * @return 医学数据集实体
     */
    DataMedicine getDataMedicineById(Long medicineId);

    /**
     * 导入医学数据集
     *
     * @param dataMedicineImportDTO 导入医学数据集参数
     * @return boolean 导入是否成功
     */
    boolean importDataMedicine(DataMedicineImportDTO dataMedicineImportDTO);

    /**
     * 创建医学数据集
     *
     * @param dataMedicineCreateDTO 创建医学数据集参数
     * @return Long 医学数据集ID
     */
    Long create(DataMedicineCreateDTO dataMedicineCreateDTO);

    /**
     * 更新医学数据集
     *
     * @param dataMedicine 医学数据集
     */
    void updateByMedicineId(DataMedicine dataMedicine);

    /**
     * 删除数据集
     *
     * @param dataMedicineDeleteDTO 删除数据集参数
     * @return boolean 是否删除成功
     */
    boolean delete(DataMedicineDeleteDTO dataMedicineDeleteDTO);

    /**
     * 医学数据集查询
     *
     * @param dataMedicineQueryDTO  查询条件
     * @return MapMap<String, Object> 查询出对应的数据集
     */
    Map<String, Object> listVO(DataMedicineQueryDTO dataMedicineQueryDTO);

    /**
     * 医学数据集详情
     *
     * @param medicalId 医学数据集ID
     * @return DataMedicineVO 医学数据集VO
     */
    DataMedicineVO get(Long medicalId);

    /**
     * 根据医学数据集Id获取完成标注文件
     *
     * @param medicalId 医学数据集ID
     * @return JSONObject 标注文件
     */
    JSONObject getFinished(Long medicalId);

    /**
     * 根据医学数据集Id获取自动标注文件
     *
     * @param medicalId 医学数据集ID
     * @return DataMedicineCompleteAnnotationVO 标注文件
     */
    DataMedicineCompleteAnnotationVO getAuto(Long medicalId);

    /**
     * 根据医学数据集Id修改数据集
     *
     * @param dataMedcineUpdateDTO 医学数据集修改DTO
     * @param medicineId           医学数据集Id
     * @return boolean  修改是否成功
     */
    boolean update(DataMedcineUpdateDTO dataMedcineUpdateDTO, Long medicineId);

    /**
     * 检测是否为公共数据集
     *
     * @param id 数据集ID
     * @param type 校验类型
     * @return Boolean 更新结果
     */
    Boolean checkPublic(Long id, OperationTypeEnum type);

    /**
     * 检测是否为公共数据集
     *
     * @param dataMedicine 数据集
     * @param type 操作类型枚举
     * @return Boolean 更新结果
     */
    Boolean checkPublic(DataMedicine dataMedicine, OperationTypeEnum type);

    /**
     * 数据集还原
     *
     * @param dto 数据清理参数
     */
    void allRollback(RecycleCreateDTO dto);


    /**
     * 根据医学数据集Id删除数据
     *
     * @param datasetId         医学数据集Id
     */
    void deleteByDatasetId(Long datasetId);
}
