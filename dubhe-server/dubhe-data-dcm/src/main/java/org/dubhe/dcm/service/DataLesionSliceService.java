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

import org.dubhe.dcm.domain.dto.DataLesionSliceCreateDTO;
import org.dubhe.dcm.domain.dto.DataLesionSliceDeleteDTO;
import org.dubhe.dcm.domain.dto.DataLesionSliceUpdateDTO;
import org.dubhe.dcm.domain.entity.DataLesionSlice;
import org.dubhe.dcm.domain.vo.DataLesionSliceVO;

import java.util.List;

/**
 * @description 病灶信息文件服务类
 * @date 2020-12-22
 */
public interface DataLesionSliceService {

    /**
     * 保存病灶信息
     *
     * @param dataLesionSliceCreateDTOS  病灶层面信息列表
     * @param medicineId                 数据集ID
     * @return boolean 数据是否插入成功
     */
    boolean save(List<DataLesionSliceCreateDTO> dataLesionSliceCreateDTOS,Long medicineId);

    /**
     * 批量插入病灶信息
     *
     * @param dataLesionSliceList 病灶信息list
     * @return boolean 数据是否插入成功
     */
    boolean insetDataLesionSliceBatch(List<DataLesionSlice> dataLesionSliceList);

    /**
     * 获取病灶信息
     *
     * @param medicineId 数据集ID
     * @return List<DataLesionSliceVO> 病灶信息list
     */
    List<DataLesionSliceVO> get(Long medicineId);

    /**
     * 删除病灶信息
     *
     * @param dataLesionSliceDeleteDTO 病灶信息删除DTO
     * @return boolean 是否删除成功
     */
    boolean delete(DataLesionSliceDeleteDTO dataLesionSliceDeleteDTO);

    /**
     * 修改病灶信息
     *
     * @param dataLesionSliceUpdateDTO 病灶信息更新DTO
     * @return boolean 是否修改成功
     */
    boolean update(DataLesionSliceUpdateDTO dataLesionSliceUpdateDTO);

    /**
     * 保存时根据数据集Id清空病灶信息
     *
     * @param medicineId 数据集ID
     * @return boolean      是否删除成功
     */
    boolean deleteByMedicineId(Long medicineId);
}
