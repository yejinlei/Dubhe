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
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.admin.domain.entity.*;

import java.util.List;
import java.util.Set;

/**
 * @description 权限组mapper接口
 * @date 2021-05-14
 */
public interface AuthCodeMapper extends BaseMapper<Auth> {


    /**
     * 根据角色id查询对应的权限组
     *
     * @param queryWrapper role的wrapper对象
     * @return 角色集合
     */
    @Select("select * from role ${ew.customSqlSegment}")
    @Results(id = "roleMapperResults",
            value = {
                    @Result(property = "id", column = "id"),
                    @Result(property = "auths",
                            column = "id",
                            many = @Many(select = "org.dubhe.admin.dao.AuthCodeMapper.selectByRoleId", fetchType = FetchType.LAZY))})
    List<Role> selectCollList(@Param("ew") Wrapper<Role> queryWrapper);


    /**
     * 给权限组绑定具体的权限
     *
     * @param list 权限列表
     */
    void tiedWithPermission(List<AuthPermission> list);

    /**
     * 清空指定权限组的权限
     *
     * @param authId 权限组id
     */
    @Delete("delete from auth_permission where auth_id=#{authId} ")
    void untiedWithPermission(Long authId);

    @Delete("<script>" +
            "delete from auth_permission where permission_id in" +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    void untiedByPermissionId(@Param("ids") Set<Long> ids);

    /**
     * 绑定角色权限
     *
     * @param roleAuths 角色权限关联DTO
     */
    void tiedRoleAuth(List<RoleAuth> roleAuths);

    /**
     * 根据角色ID解绑角色权限
     *
     * @param roleId 角色ID
     */
    @Update("delete from roles_auth where role_id = #{roleId}")
    void untiedRoleAuthByRoleId(Long roleId);

    /**
     * 通过权限组id获取权限
     *
     * @param authId 权限组id
     * @return List<Permission> 权限列表
     */
    @Select("select * from permission where id in (select permission_id from auth_permission where auth_id=#{authId}) and deleted=0 order by id")
    List<Permission> getPermissionByAuthId(Long authId);

    /**
     * 根据角色id获取绑定的权限组列表
     *
     * @param roleId 角色id
     * @return 权限组列表
     */
    @Select("select * from auth  where id in (select auth_id from roles_auth where role_id=#{roleId}) and deleted=0")
    List<Auth> selectByRoleId(long roleId);

}
