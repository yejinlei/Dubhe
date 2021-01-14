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

package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.dubhe.data.domain.entity.Task;

/**
 * @description 标注任务信息 Mapper 接口
 * @date 2020-04-10
 */
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 任务完成数加一
     *
     * @param id            文件ID
     * @param filesCount    文件数量
     * @return int   执行次数
     */
    @Update("update data_task t set t.finished = t.finished + #{filesCount} where t.id = #{id}")
    int finishFile(@Param("id") Long id, @Param("filesCount") Integer filesCount);

    /**
     * 修改完成任务数量
     *
     * @param id      文件ID
     * @param fileNum 文件数量
     * @return int    执行次数
     */
    @Update("update data_task t set t.finished = t.finished + #{fileNum} WHERE t.id = #{id}")
    int finishFileNum(@Param("id") Long id, @Param("fileNum") Integer fileNum);

}
