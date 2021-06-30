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
package org.dubhe.admin.service;

import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.Menu;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 菜单服务 Service
 * @date 2020-06-01
 */
public interface MenuService {

    /**
     * 按条件查询菜单列表
     *
     * @param criteria 菜单请求实体
     * @return java.util.List<org.dubhe.domain.dto.MenuDTO> 菜单返回实例
     */
    List<MenuDTO> queryAll(MenuQueryDTO criteria);

    /**
     * 根据id查询菜单信息
     *
     * @param id 菜单id
     * @return org.dubhe.domain.dto.MenuDTO 菜单返回实例
     */
    MenuDTO findById(long id);

    /**
     * 新增菜单
     *
     * @param resources 菜单新增请求实体
     * @return org.dubhe.domain.dto.MenuDTO 菜单返回实例
     */
    MenuDTO create(MenuCreateDTO resources);

    /**
     * 修改菜单
     *
     * @param resources 菜单修改请求实体
     */
    void update(MenuUpdateDTO resources);

    /**
     * 查询可删除的菜单
     *
     * @param menuList
     * @param menuSet
     * @return java.util.Set<org.dubhe.domain.entity.Menu>
     */
    Set<Menu> getDeleteMenus(List<Menu> menuList, Set<Menu> menuSet);

    /**
     * 获取菜单树
     *
     * @param menus 菜单列表
     * @return java.lang.Object 菜单树结构列表
     */
    Object getMenuTree(List<Menu> menus);

    /**
     * 根据ID获取菜单列表
     *
     * @param pid id
     * @return java.util.List<org.dubhe.domain.entity.Menu> 菜单返回列表
     */
    List<Menu> findByPid(long pid);

    /**
     * 构建菜单树
     *
     * @param menuDtos 菜单请求实体
     * @return java.util.Map<java.lang.String, java.lang.Object>  菜单树结构
     */
    Map<String, Object> buildTree(List<MenuDTO> menuDtos);

    /**
     * 根据角色查询菜单列表
     *
     * @param roles 角色
     * @return java.util.List<org.dubhe.domain.dto.MenuDTO> 菜单返回实例
     */
    List<MenuDTO> findByRoles(List<RoleSmallDTO> roles);

    /**
     * 构建菜单树
     *
     * @param menuDtos 菜单请求实体
     * @return java.util.List<org.dubhe.domain.vo.MenuVo> 菜单树返回实例
     */
    Object buildMenus(List<MenuDTO> menuDtos);

    /**
     * 获取菜单
     *
     * @param id id
     * @return org.dubhe.domain.entity.Menu 菜单返回实例
     */
    Menu findOne(Long id);

    /**
     * 删除菜单
     *
     * @param menuSet 删除菜单请求集合
     */
    void delete(Set<Menu> menuSet);

    /**
     * 导出
     *
     * @param queryAll 待导出的数据
     * @param response 导出http响应
     * @throws IOException 导出异常
     */
    void download(List<MenuDTO> queryAll, HttpServletResponse response) throws IOException;
}
