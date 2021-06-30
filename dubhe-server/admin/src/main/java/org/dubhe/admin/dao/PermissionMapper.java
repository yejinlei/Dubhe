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
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.admin.domain.entity.Permission;
import org.dubhe.admin.domain.entity.Role;
import org.dubhe.biz.base.dto.SysPermissionDTO;

import java.util.List;

/**
 * @description 角色权限关联mapper
 * @date 2021-04-26
 */
public interface PermissionMapper extends BaseMapper<Permission> {


    /**
     * 查询实体及关联对象
     *
     * @param queryWrapper 角色wrapper对象
     * @return 角色列表
     */
    @Select("select * from role ${ew.customSqlSegment}")
    @Results(id = "roleMapperResults",
            value = {
                    @Result(property = "id", column = "id"),
                    @Result(property = "Permissions",
                            column = "id",
                            many = @Many(select = "org.dubhe.admin.dao.PermissionMapper.selectByRoleId", fetchType = FetchType.LAZY))})
    List<Role> selectCollList(@Param("ew") Wrapper<Role> queryWrapper);


    /**
     * 绑定角色权限
     *
     * @param roleId 角色ID
     * @param menuId 权限ID
     */
    @Update("insert into roles_auth values (#{roleId}, #{menuId})")
    void tiedRoleAuth(Long roleId, Long menuId);


    /**
     * 根据roleId查询权限列表
     *
     * @param roleId roleId
     * @return List<Permission> 权限列表
     */
    @Select("select p.permission, p.name from permission p left join auth_permission ap on p.id = ap.permission_id left join roles_auth ra on ap.auth_id = ra.auth_id where ra.role_id=#{roleId} and deleted=0 ")
    List<Permission> selectByRoleId(long roleId);

    /**
     * 根据权限的 PID 查询
     *
     * @param pid 父权限id
     * @return List<Permission> 权限列表
     */
    @Select("select * from permission where  pid=#{pid} and deleted = 0 ")
    List<Permission> findByPid(long pid);


    /**
     * 根据角色id查询用户权限
     *
     * @param roleIds 用户id
     * @return 权限信息
     */
    List<SysPermissionDTO> selectPermissinByRoleIds(@Param("list") List<Long> roleIds);

    /**
     * 根据权限名获取权限信息
     *
     * @param name 权限名称
     * @return 权限
     */
    @Select("select * from permission where name=#{name} and deleted=0")
    Permission findByName(String name);


}







