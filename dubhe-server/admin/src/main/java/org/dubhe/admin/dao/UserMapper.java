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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.admin.domain.entity.User;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description Demo服务mapper
 * @date 2020-11-26
 */
public interface UserMapper extends BaseMapper<User> {


    /**
     * 根据ID查询实体及关联对象
     *
     * @param id 用户id
     * @return 用户
     */
    @Select("select * from user where id=#{id} and deleted = 0")
    @Results(id = "userMapperResults",
            value = {
                    @Result(property = "id", column = "id"),
                    @Result(property = "roles",
                            column = "id",
                            many = @Many(select = "org.dubhe.admin.dao.RoleMapper.findRolesByUserId",
                                    fetchType = FetchType.LAZY)),
                    @Result(property = "userAvatar",
                            column = "avatar_id",
                            one = @One(select = "org.dubhe.admin.dao.UserAvatarMapper.selectById",
                                    fetchType = FetchType.LAZY))})
    User selectCollById(Long id);

    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return 用户
     */
    @Select("select * from user where  username=#{username} and deleted = 0")
    @ResultMap(value = "userMapperResults")
    User findByUsername(String username);

    /**
     * 根据邮箱查询
     *
     * @param email 邮箱
     * @return 用户
     */
    @Select("select * from user where  email=#{email} and deleted = 0")
    @ResultMap(value = "userMapperResults")
    User findByEmail(String email);

    /**
     * 修改密码
     *
     * @param username              用户名
     * @param pass                  密码
     * @param lastPasswordResetTime 密码最后一次重置时间
     */

    @Update("update user set password = #{pass} , last_password_reset_time = #{lastPasswordResetTime} where username = #{username}")
    void updatePass(String username, String pass, Date lastPasswordResetTime);

    /**
     * 修改邮箱
     *
     * @param username 用户名
     * @param email    邮箱
     */
    @Update("update user set email = #{email} where username = #{username}")
    void updateEmail(String username, String email);

    /**
     * 查找用户权限
     *
     * @param userId 用户id
     * @return 权限集合
     */
    Set<String> queryPermissionByUserId(Long userId);


    /**
     * 查询实体及关联对象
     *
     * @param queryWrapper 用户wrapper对象
     * @return 用户集合
     */
    @Select("select * from user ${ew.customSqlSegment}")
    @ResultMap(value = "userMapperResults")
    List<User> selectCollList(@Param("ew") Wrapper<User> queryWrapper);

    /**
     * 分页查询实体及关联对象
     *
     * @param page 分页对象
     * @param queryWrapper 用户wrapper对象
     * @return 分页user集合
     */
    @Select("select * from user ${ew.customSqlSegment} order by id desc")
    @ResultMap(value = "userMapperResults")
    IPage<User> selectCollPage(Page<User> page, @Param("ew") Wrapper<User> queryWrapper);

    /**
     * 根据角色分页查询实体及关联对象
     *
     * @param page 分页对象
     * @param queryWrapper 用户wrapper对象
     * @param roleId 角色id
     * @return 分页用户集合
     */
    @Select("select u.* from user u join  users_roles ur on u.id=ur.user_id and ur.role_id=#{roleId} ${ew.customSqlSegment}  order by u.id desc")
    @ResultMap(value = "userMapperResults")
    IPage<User> selectCollPageByRoleId(Page<User> page, @Param("ew") Wrapper<User> queryWrapper, Long roleId);

}
