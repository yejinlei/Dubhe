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


import org.dubhe.dcm.domain.dto.MedicineAnnotationDTO;
import org.dubhe.dcm.domain.dto.MedicineAutoAnnotationDTO;
import org.dubhe.dcm.domain.entity.DataMedicineFile;
import org.dubhe.dcm.domain.vo.ScheduleVO;

import java.util.List;
import java.util.Map;

/**
 * @description 医学标注服务
 * @date 2020-11-16
 */
public interface MedicineAnnotationService {

    /**
     * 医学自动标注
     *
     * @param medicineAutoAnnotationDTO 医学自动标注DTO
     */
    void auto(MedicineAutoAnnotationDTO medicineAutoAnnotationDTO);

    /**
     * 医学自动标注完成
     *
     * @return boolean 是否有任务
     */
    boolean finishAuto();

    /**
     * 标注保存
     *
     * @param medicineAnnotationDTO 医学标注DTO
     * @return 保存是否成功
     */
    boolean save(MedicineAnnotationDTO medicineAnnotationDTO);

    /**
     * 查询数据集标注的进度
     *
     * @param ids 要查询的数据集ID
     * @return 进度
     */
    Map<String, ScheduleVO> schedule(List<Long> ids);

    /**
     * 合并自动标注后的JSON文件
     *
     * @param medicineId        医学数据集ID
     * @param dataMedicineFiles 医学数据集文件列表
     */
    void mergeAnnotation(Long medicineId, List<DataMedicineFile> dataMedicineFiles);
}
