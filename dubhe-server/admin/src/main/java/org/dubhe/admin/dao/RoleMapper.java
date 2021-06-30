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
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.admin.dao.provider.RoleProvider;
import org.dubhe.admin.domain.entity.Role;

import java.io.Serializable;
import java.util.List;

/**
 * @description 角色 mapper
 * @date 2020-11-26
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户id查询用户角色
     *
     * @param userId 用户id
     * @return 用户信息
     */
    @Select("select sr.id, sr.name, sur.user_id from  users_roles sur left join role sr on sr.id = sur.role_id  where  sur.user_id = #{userId} and sr.deleted = 0 ")
    List<Role> selectRoleByUserId(@Param("userId") Long userId);


    /**
     * 查询实体及关联对象
     *
     * @param queryWrapper 角色wrapper对象
     * @return List<Role> 角色列表
     */
    @Select("select * from role ${ew.customSqlSegment}")
    @Results(id = "roleMapperResults",
            value = {
                    @Result(property = "id", column = "id"),
                    @Result(property = "menus",
                            column = "id",
                            many = @Many(select = "org.dubhe.admin.dao.MenuMapper.selectByRoleId", fetchType = FetchType.LAZY)),
                    @Result(property = "auths",
                            column = "id",
                            many = @Many(select = "org.dubhe.admin.dao.AuthCodeMapper.selectByRoleId", fetchType = FetchType.LAZY))})
    List<Role> selectCollList(@Param("ew") Wrapper<Role> queryWrapper);

    /**
     * 分页查询实体及关联对象
     *
     * @param page 分页对象
     * @param queryWrapper 角色wrapper对象
     * @return IPage<Role> 分页角色集合
     */
    @Select("select * from role ${ew.customSqlSegment}")
    @ResultMap(value = "roleMapperResults")
    IPage<Role> selectCollPage(Page<Role> page, @Param("ew") Wrapper<Role> queryWrapper);

    /**
     * 根据ID查询实体及关联对象
     *
     * @param id 序列id
     * @return 角色
     */
    @Select("select * from role where id=#{id} and deleted = 0 ")
    @ResultMap("roleMapperResults")
    Role selectCollById(Serializable id);

    /**
     * 根据名称查询
     *
     * @param name 角色名称
     * @return 角色
     */
    @Select("select *  from role where name = #{name} and deleted = 0 ")
    Role findByName(String name);

    /**
     * 绑定用户角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    @Update("insert into users_roles values (#{userId}, #{roleId})")
    void tiedUserRole(Long userId, Long roleId);

    /**
     * 根据用户ID解绑用户角色
     *
     * @param userId 用户ID
     */
    @Update("delete from users_roles where user_id = #{userId}")
    void untiedUserRoleByUserId(Long userId);

    /**
     * 根据角色ID解绑用户角色
     *
     * @param roleId 角色ID
     */
    @Update("delete from users_roles where role_id = #{roleId}")
    void untiedUserRoleByRoleId(Long roleId);

    /**
     * 绑定角色菜单
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     */
    @Update("insert into roles_menus values (#{roleId}, #{menuId})")
    void tiedRoleMenu(Long roleId, Long menuId);

    /**
     * 根据角色ID解绑角色菜单
     *
     * @param roleId 角色ID
     */
    @Update("delete from roles_menus where role_id = #{roleId}")
    void untiedRoleMenuByRoleId(Long roleId);

    /**
     * 根据菜单ID解绑角色菜单
     *
     * @param menuId 菜单ID
     */
    @Update("delete from roles_menus where menu_id = #{menuId}")
    void untiedRoleMenuByMenuId(Long menuId);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return List<Role> 角色列表
     */
    @SelectProvider(type = RoleProvider.class, method = "findRolesByUserId")
    List<Role> findRolesByUserId(Long userId);

    /**
     * 根据团队ID和用户ID查询角色
     *
     * @param userId 用户ID
     * @param teamId 团队ID
     * @return List<Role> 角色列表
     */
    @SelectProvider(type = RoleProvider.class, method = "findByUserIdAndTeamId")
    List<Role> findByUserIdAndTeamId(Long userId, Long teamId);
}
