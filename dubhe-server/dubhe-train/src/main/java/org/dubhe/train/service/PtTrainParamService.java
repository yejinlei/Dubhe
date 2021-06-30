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

package org.dubhe.train.service;


import org.dubhe.train.domain.dto.PtTrainParamCreateDTO;
import org.dubhe.train.domain.dto.PtTrainParamDeleteDTO;
import org.dubhe.train.domain.dto.PtTrainParamQueryDTO;
import org.dubhe.train.domain.dto.PtTrainParamUpdateDTO;

import java.util.List;
import java.util.Map;

/**
 * @description 任务参数 服务类
 * @date 2020-04-27
 */
public interface PtTrainParamService {

    /**
     * 任务参数列表展示
     *
     * @param ptTrainParamQueryDTO 任务参数列表展示条件
     * @return Map<String, Object>  任务参数列表分页数据
     **/
    Map<String, Object> getTrainParam(PtTrainParamQueryDTO ptTrainParamQueryDTO);

    /**
     * 保存任务参数
     *
     * @param ptTrainParamCreateDTO 保存任务参数条件
     * @return List<Long>  保存任务参数id集合
     **/
    List<Long> createTrainParam(PtTrainParamCreateDTO ptTrainParamCreateDTO);

    /**
     * 修改任务参数
     *
     * @param ptTrainParamUpdateDTO 修改任务参数条件
     * @return List<Long>  修改任务参数id集合
     **/
    List<Long> updateTrainParam(PtTrainParamUpdateDTO ptTrainParamUpdateDTO);

    /**
     * 删除任务参数
     *
     * @param ptTrainParamDeleteDTO 删除任务参数条件
     **/
    void deleteTrainParam(PtTrainParamDeleteDTO ptTrainParamDeleteDTO);

}
