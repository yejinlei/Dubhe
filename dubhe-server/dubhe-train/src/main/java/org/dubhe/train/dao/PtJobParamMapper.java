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

package org.dubhe.train.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.train.domain.entity.PtJobParam;

/**
 * @description job运行参数及结果
 * @date 2020-04-27
 */
public interface PtJobParamMapper extends BaseMapper<PtJobParam> {

    /**
     * 根据id修改运行开始时间
     * @param id 训练参数id
     * @param runStartTime 运行开始时间
     * @return 数量
     */
    @Update("update pt_job_param set run_start_time = #{runStartTime} where id = #{id}")
    int updateRunStartTimeById(@Param("id") Long id, @Param("runStartTime") Long runStartTime);

}
