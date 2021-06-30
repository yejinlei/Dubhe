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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.dubhe.dcm.domain.dto.DataMedicineFileCreateDTO;
import org.dubhe.dcm.domain.entity.DataMedicine;
import org.dubhe.dcm.domain.entity.DataMedicineFile;

import java.util.List;

/**
 * @description 医学数据集文件服务类
 * @date 2020-11-12
 */
public interface DataMedicineFileService {

    /**
     * 根据条件获取医学文件数量
     *
     * @param queryWrapper  医学文件查询条件
     * @return  医学文件数量
     */
    Integer getCountByMedicineId(QueryWrapper<DataMedicineFile> queryWrapper);

    /**
     * 插入医学数据集相关文件数据
     *
     * @param dataMedicineFileList 文件路径
     * @param dataMedicine    医学数据集
     */
    void save(List<DataMedicineFileCreateDTO> dataMedicineFileList, DataMedicine dataMedicine);

    /**
     * 获取医学文件列表
     *
     * @param wrapper 查询条件
     * @return
     */
    List<DataMedicineFile> listFile(QueryWrapper<DataMedicineFile> wrapper);


    /**
     * 补充文件详情后进行排序
     *
     * @param dataMedicineFiles  医学文件列表
     * @param medicineId         医学数据集ID
     * @return List<DataMedicineFile> 排序后的医学文件列表
     */
    List<DataMedicineFile> insertInstanceAndSort(List<DataMedicineFile> dataMedicineFiles,Long medicineId);

    /**
     * 更新修改人ID
     *
     * @param medicineId 医学数据集id
     */
    void updateUserIdByMedicineId(Long medicineId);

    /**
     * 根据医学数据集Id修改文件状态
     *
     * @param id         医学数据集Id
     * @param deleteFlag 删除标识
     */
    void updateStatusById(Long id, Boolean deleteFlag);

    /**
     * 根据医学数据集Id删除数据
     *
     * @param datasetId         医学数据集Id
     */
    void deleteByDatasetId(Long datasetId);
}
