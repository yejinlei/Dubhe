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
package org.dubhe.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.admin.domain.entity.UserConfig;
import org.dubhe.admin.domain.vo.UserLimitConfigVO;
import org.dubhe.biz.base.vo.UserAllotResourceVO;
import org.dubhe.biz.base.vo.UserAllotVO;

import java.util.List;

/**
 * @description 用户配置 Mapper
 * @date 2021-6-30
 */
public interface UserConfigMapper extends BaseMapper<UserConfig> {

    /**
     * 插入或更新配置
     * @param userConfig 用户配置
     */
    Long insertOrUpdate(UserConfig userConfig);

    /**
     * 统计内存、CPU配额
     */
    @Select("select sum(memory_limit) memoryAllotTotal,sum(cpu_limit) cpuAllotTotal from  user_config where deleted=0;")
    UserAllotResourceVO selectResourceSum();

    /**
     * 统计CPU配额Top10
     */
    @Select("select u.username,uc.cpu_limit allotTotal from user_config uc, user u where uc.user_id=u.id and uc.deleted=0 order by cpu_limit desc limit 10;")
    List<UserAllotVO> selectCpuAllotTotal();

    /**
     * 统计内存配额Top10
     */
    @Select("select u.username,uc.memory_limit allotTotal from user_config uc, user u where uc.user_id=u.id and uc.deleted=0 order by memory_limit desc limit 10;")
    List<UserAllotVO> selectMemoryAllotTotal();

    /**
     * 根据用户id查询资源配额（cpu、memory）
     *
     * @param userId 用户ID
     * @return 资源配额实体
     */
    @Select("select * from user_config where user_id=#{userId} AND deleted=0")
    UserConfig selectLimitSumByUser(@Param("userId") Long userId);

    /**
     * 分页查询资源列表
     *
     * @param page 分页对象
     * @param sort 排序字段
     * @param order 排序方式
     * @return 用户配额列表
     */
    List<UserLimitConfigVO> selectLimitSum(Page page,
                                           @Param("sort") String sort,
                                           @Param("order") String order);
}
