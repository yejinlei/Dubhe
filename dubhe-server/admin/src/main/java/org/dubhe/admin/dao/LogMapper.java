/**
 * Copyright 2019-2020 Zheng Jie
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
 */
package org.dubhe.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.dubhe.admin.domain.entity.Log;

/**
 * @description  获取一个时间段的IP记录
 * @date 2020-03-25
 */
public interface LogMapper extends BaseMapper<Log> {
    /**
     * 获取一个时间段的IP记录
     *
     * @param date1 startTime
     * @param date2 entTime
     * @return IP数目
     */
    @Select("select count(*) FROM (select request_ip FROM log where create_time between #{date1} and #{date2} GROUP BY request_ip) as s")
    Long findIp(String date1, String date2);

    /**
     * 根据日志类型删除信息
     *
     * @param logType 日志类型
     */
    @Delete("delete from log where log_type = #{logType}")
    void deleteByLogType(String logType);
}
