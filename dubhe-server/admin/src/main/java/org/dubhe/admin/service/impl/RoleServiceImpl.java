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
package org.dubhe.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.admin.dao.RoleMapper;
import org.dubhe.admin.domain.dto.RoleCreateDTO;
import org.dubhe.admin.domain.dto.RoleDTO;
import org.dubhe.admin.domain.dto.RoleQueryDTO;
import org.dubhe.admin.domain.dto.RoleSmallDTO;
import org.dubhe.admin.domain.dto.RoleUpdateDTO;
import org.dubhe.admin.domain.entity.Menu;
import org.dubhe.admin.domain.entity.Role;
import org.dubhe.admin.service.RoleService;
import org.dubhe.admin.service.convert.RoleConvert;
import org.dubhe.admin.service.convert.RoleSmallConvert;
import org.dubhe.biz.base.constant.UserConstant;
import org.dubhe.biz.base.enums.BaseErrorCodeEnum;
import org.dubhe.biz.base.enums.SwitchEnum;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.db.utils.PageUtil;
import org.dubhe.biz.db.utils.WrapperHelp;
import org.dubhe.biz.file.utils.DubheFileUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @description 角色服务 实现类
 * @date 2020-06-01
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleConvert roleConvert;

    @Autowired
    private RoleSmallConvert roleSmallConvert;

    /**
     * 按条件查询角色信息
     *
     * @param criteria 角色查询条件
     * @return java.util.List<org.dubhe.domain.dto.RoleSmallDTO> 角色信息返回实例
     */
    @Override
    public List<RoleSmallDTO> queryAllSmall(RoleQueryDTO criteria) {
        return roleSmallConvert.toDto(roleMapper.selectList((WrapperHelp.getWrapper(criteria))));
    }


    /**
     * 按条件查询角色列表
     *
     * @param criteria 角色查询条件
     * @return java.util.List<org.dubhe.domain.dto.RoleDTO> 角色信息返回实例
     */
    @Override
    public List<RoleDTO> queryAll(RoleQueryDTO criteria) {
        return roleConvert.toDto(roleMapper.selectCollList(WrapperHelp.getWrapper(criteria)));
    }

    /**
     * 分页查询角色列表
     *
     * @param criteria 角色查询条件
     * @param page     分页实体
     * @return java.lang.Object 角色信息返回实例
     */
    @Override
    public Object queryAll(RoleQueryDTO criteria, Page page) {
        IPage<Role> roles = roleMapper.selectCollPage(page, WrapperHelp.getWrapper(criteria));
        return PageUtil.toPage(roles, roleConvert::toDto);
    }

    /**
     * 根据ID查询角色信息
     *
     * @param id id
     * @return org.dubhe.domain.dto.RoleDTO 角色信息
     */
    @Override
    public RoleDTO findById(long id) {
        Role role = roleMapper.selectCollById(id);
        return roleConvert.toDto(role);
    }

    /**
     * 新增角色
     *
     * @param resources 角色新增请求实体
     * @return org.dubhe.domain.dto.RoleDTO 角色返回实例
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleDTO create(RoleCreateDTO resources) {
        if (!Objects.isNull(roleMapper.findByName(resources.getName()))) {
            throw new BusinessException("角色名已存在");
        }
        Role role = Role.builder().build();
        BeanUtils.copyProperties(resources, role);
        roleMapper.insert(role);
        //新创建角色分配默认的【概览】菜单
        roleMapper.tiedRoleMenu(role.getId(), 1L);
        return roleConvert.toDto(role);
    }


    /**
     * 修改角色
     *
     * @param resources 角色修改请求实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(RoleUpdateDTO resources) {

        Role roleTmp = roleMapper.findByName(resources.getName());
        if (!Objects.isNull(roleTmp) && !roleTmp.getId().equals(resources.getId())) {
            throw new BusinessException("角色名已存在");
        }

        Role role = Role.builder().build();
        BeanUtils.copyProperties(resources, role);
        roleMapper.updateById(role);
    }


    /**
     * 修改角色菜单
     *
     * @param resources 角色菜单请求实体
     * @param roleDTO   角色请求实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(RoleUpdateDTO resources, RoleDTO roleDTO) {
        Role role = roleConvert.toEntity(roleDTO);
        role.setMenus(resources.getMenus());
        roleMapper.untiedRoleMenuByRoleId(role.getId());
        for (Menu menu : resources.getMenus()) {
            roleMapper.tiedRoleMenu(role.getId(), menu.getId());
        }
    }

    /**
     * 删除角色菜单
     *
     * @param id 角色id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void untiedMenu(Long id) {
        roleMapper.untiedRoleMenuByMenuId(id);
    }

    /**
     * 批量删除角色
     *
     * @param ids 角色ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {

        if (!CollectionUtils.isEmpty(ids)) {
            // admin 角色和 register 角色不可删除
            if (ids.contains(Long.valueOf(UserConstant.ADMIN_ROLE_ID)) ||
                    ids.contains(Long.valueOf(UserConstant.REGISTER_ROLE_ID))) {
                throw new BusinessException(BaseErrorCodeEnum.SYSTEM_ROLE_CANNOT_DELETE);
            }

            for (Long id : ids) {
                roleMapper.untiedUserRoleByRoleId(id);
                roleMapper.untiedRoleMenuByRoleId(id);
                roleMapper.updateById(
                        Role.builder()
                                .id(id)
                                .deleted(SwitchEnum.getBooleanValue(SwitchEnum.ON.getValue())).build()
                );
            }

        }


    }

    /**
     * 导出角色信息
     *
     * @param roles    角色列表
     * @param response
     */
    @Override
    public void download(List<RoleDTO> roles, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RoleDTO role : roles) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("角色名称", role.getName());
            map.put("默认权限", role.getPermission());
            map.put("描述", role.getRemark());
            map.put("创建日期", role.getCreateTime());
            list.add(map);
        }
        DubheFileUtil.downloadExcel(list, response);
    }


    /**
     * 根据用户ID获取角色信息
     *
     * @param userId 用户ID
     * @return java.util.List<org.dubhe.domain.dto.RoleSmallDTO> 角色列表
     */
    @Override
    public List<RoleSmallDTO> getRoleByUserId(Long userId) {
        List<Role> list = roleMapper.findRolesByUserId(userId);
        return roleSmallConvert.toDto(list);
    }


    /**
     * 获取角色列表
     *
     * @param userId 用户ID
     * @param teamId 团队ID
     * @return java.util.List<org.dubhe.domain.dto.RoleSmallDTO> 角色列表
     */
    @Override
    public List<RoleSmallDTO> getRoleByUserIdAndTeamId(Long userId, Long teamId) {
        List<Role> list = roleMapper.findByUserIdAndTeamId(userId, teamId);
        return roleSmallConvert.toDto(list);
    }

    /**
     * 新增角色菜单
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     */
    @Override
    public void tiedRoleMenu(Long roleId, Long menuId) {
        roleMapper.tiedRoleMenu(roleId, menuId);
    }

}
