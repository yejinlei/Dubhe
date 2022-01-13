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
package org.dubhe.tadl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.dubhe.tadl.domain.entity.Trial;
import org.dubhe.tadl.domain.entity.TrialData;

import java.util.List;

/**
 * @description 试验运行结果服务Mapper
 * @date 2021-03-22
 */
public interface TrialDataMapper extends BaseMapper<TrialData>{

    /**
     * 批量写入 trial data
     *
     * @param trialDataList trial 列表
     */
    void saveList(@Param("trialDataList") List<TrialData> trialDataList);

    /**
     * 更新最大值
     * @param trialId
     * @param value
     * @return
     */
    int updateValue(@Param("trialId") Long trialId,@Param("value") Double value);
}
