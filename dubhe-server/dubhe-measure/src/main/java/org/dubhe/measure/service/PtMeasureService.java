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
package org.dubhe.measure.service;

import org.dubhe.measure.domain.dto.PtMeasureCreateDTO;
import org.dubhe.measure.domain.dto.PtMeasureDeleteDTO;
import org.dubhe.measure.domain.dto.PtMeasureQueryDTO;
import org.dubhe.measure.domain.dto.PtMeasureUpdateDTO;
import org.dubhe.recycle.domain.dto.RecycleCreateDTO;

import java.util.Map;

/**
 * @description 度量管理service
 * @date 2020-11-16
 */
public interface PtMeasureService {


    /**
     * 查询度量信息
     *
     * @param ptMeasureQueryDTO 查询条件
     * @return Map<String, Object> 度量列表分页信息
     */
    Map<String, Object> getMeasure(PtMeasureQueryDTO ptMeasureQueryDTO);

    /**
     * 新建度量
     *
     * @param ptMeasureCreateDTO 新建度量入参DTO
     */
    void createMeasure(PtMeasureCreateDTO ptMeasureCreateDTO);

    /**
     * 修改度量
     *
     * @param ptMeasureUpdateDTO 修改度量入参DTO
     */
    void updateMeasure(PtMeasureUpdateDTO ptMeasureUpdateDTO);

    /**
     * 根据id删除度量
     *
     * @param ptMeasureDeleteDTO 删除度量的条件DTO
     */
    void deleteMeasure(PtMeasureDeleteDTO ptMeasureDeleteDTO);

    /**
     * 根据度量名称返回度量文件信息
     *
     * @param name 度量名称
     * @return String 度量文件json字符串
     */
    String getMeasureByName(String name);

    /**
     * 度量文件回收还原
     *
     * @param dto 还原DTO对象
     */
    void recycleRollback(RecycleCreateDTO dto);
}
