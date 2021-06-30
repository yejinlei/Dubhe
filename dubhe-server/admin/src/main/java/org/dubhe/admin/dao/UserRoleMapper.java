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
import org.apache.ibatis.annotations.Param;
import org.dubhe.admin.domain.entity.UserRole;

import java.util.List;
import java.util.Set;

/**
 * @description 用户角色 mapper
 * @date 2020-6-9
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 批量删除用户
     *
     * @param userIds 用户id集合
     */
    void deleteByUserId(@Param("list") Set<Long> userIds);

    /**
     * 批量添加用户角色
     *
     * @param userRoles 用户角色实体集合
     */
    void insertBatchs(List<UserRole> userRoles);

}

