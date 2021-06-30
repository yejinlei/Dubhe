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
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.optimize.domain.entity.ModelOptTaskInstance;

import java.util.List;

/**
 * @description 模型优化实例
 * @date 2020-05-22
 */
@DataPermission(ignoresMethod = {"insert", "markInstanceExecFailed", "selectWaitingFor5MinutesInstances"})
public interface ModelOptTaskInstanceMapper extends BaseMapper<ModelOptTaskInstance> {

    /**
     * 将实例状态标记为执行失败
     *
     * @param instId 实例id
     * @return 更新行数
     */
    @Update("update model_opt_task_instance set status = '3' where id = #{instId} and status in ('-1','0')")
    Integer markInstanceExecFailed(Long instId);

    /**
     * 获取5分钟以前状态仍为等待中的实例
     *
     * @return 实例列表
     */
    @Select("select * from model_opt_task_instance where status = '-1' and create_time < date_sub(now(), interval 5 minute)")
    List<ModelOptTaskInstance> selectWaitingFor5MinutesInstances();
}
