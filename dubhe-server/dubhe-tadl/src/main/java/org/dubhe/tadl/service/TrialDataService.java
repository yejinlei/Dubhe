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
package org.dubhe.tadl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.domain.entity.TrialData;

import java.util.List;

/**
 * @description 试验数据服务类
 * @date 2020-03-22
 */
public interface TrialDataService {

    /**
     * 获取 trial 列表
     *
     * @param wrapper 查询条件
     * @return
     */
    List<TrialData> getTrialDataList(LambdaQueryWrapper<TrialData> wrapper);

    /**
     * 根据 trial ID查询
     * @param trialId trialId
     * @return trial 数据
     */
    TrialData selectOneByTrialId(Long trialId);

    /**
     * 删除 trial data
     *
     * @param wrapper 删除实验 wrapper
     */
    void delete(LambdaQueryWrapper<TrialData> wrapper);

    /**
     * 批量写入trial data
     *
     * @param trialDataList trial 列表
     */
    void insertList(List<TrialData> trialDataList);

}
