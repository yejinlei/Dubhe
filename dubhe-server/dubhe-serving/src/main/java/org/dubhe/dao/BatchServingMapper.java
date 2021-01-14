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
package org.dubhe.dao;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.domain.entity.BatchServing;

/**
 * @description 批量模型部署配置
 * @date 2020-08-25
 */
public interface BatchServingMapper extends BaseMapper<BatchServing> {

    /**
     * 根据k8s回调状态修改批量服务状态
     *
     * @param id
     * @param status
     * @return
     */
    @Update("update serving_batch set status=#{status} where id = #{id}")
    int updateBatchServingStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 根据k8s回调状态修改批量服务开始时间
     *
     * @param id
     * @param startTime
     * @return
     */
    @Update("update serving_batch set start_time=#{startTime} where id = #{id}")
    int updateBatchServingStartTime(@Param("id") Long id, @Param("startTime") DateTime startTime);

    /**
     * 根据k8s回调状态修改批量服务结束时间
     *
     * @param id
     * @param endTime
     * @return
     */
    @Update("update serving_batch set end_time=#{endTime} , progress='100' where id = #{id}")
    int updateBatchServingEndTime(@Param("id") Long id, @Param("endTime") DateTime endTime);

    /**
     * 根据k8s回调状态修改批量服务进度
     *
     * @param id
     * @param progress
     * @return
     */
    @Update("update serving_batch set progress=#{progress} where id = #{id}")
    int updateBatchServingProgress(@Param("id") Long id, @Param("progress") String progress);
}
