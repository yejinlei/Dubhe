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

package org.dubhe.optimize.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.optimize.domain.entity.ModelOptTask;

/**
 * @description 模型优化任务
 * @date 2020-05-22
 */
@DataPermission(ignoresMethod = {"insert"})
public interface ModelOptTaskMapper extends BaseMapper<ModelOptTask> {
    /**
     * 还原回收数据
     *
     * @param id            optimize modelOptTask id
     * @param deleteFlag    删除标识
     * @return int 数量
     */
    @Update("update model_opt_task set deleted = #{deleteFlag} where id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);
}
