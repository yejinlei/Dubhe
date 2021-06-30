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

import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageCreateDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageDeleteDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageQueryDTO;
import org.dubhe.algorithm.domain.dto.PtTrainAlgorithmUsageUpdateDTO;

import java.util.List;
import java.util.Map;

/**
 * @description 算法用途 服务类
 * @date 2020-06-23
 */
public interface PtTrainAlgorithmUsageService {

    /**
     * 查询算法用途
     *
     * @param ptTrainAlgorithmUsageQueryDTO 查询算法用途参数
     */
    Map<String, Object> queryAll(PtTrainAlgorithmUsageQueryDTO ptTrainAlgorithmUsageQueryDTO);

    /**
     * 新增算法用途
     *
     * @param ptTrainAlgorithmUsageCreateDTO 新增算法用途参数
     */
    List<Long> create(PtTrainAlgorithmUsageCreateDTO ptTrainAlgorithmUsageCreateDTO);

    /**
     * 删除算法用途
     *
     * @param ptTrainAlgorithmUsageDeleteDTO 删除算法用途参数
     */
    void deleteAll(PtTrainAlgorithmUsageDeleteDTO ptTrainAlgorithmUsageDeleteDTO);

    /**
     * 更新算法用途
     *
     * @param ptTrainAlgorithmUsageUpdateDTO 更新算法用途参数
     */
    void update(PtTrainAlgorithmUsageUpdateDTO ptTrainAlgorithmUsageUpdateDTO);

}
