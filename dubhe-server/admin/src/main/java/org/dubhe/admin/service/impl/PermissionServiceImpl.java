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
package org.dubhe.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.dubhe.admin.dao.AuthCodeMapper;
import org.dubhe.admin.dao.PermissionMapper;
import org.dubhe.admin.domain.dto.PermissionCreateDTO;
import org.dubhe.admin.domain.dto.PermissionDeleteDTO;
import org.dubhe.admin.domain.dto.PermissionQueryDTO;
import org.dubhe.admin.domain.dto.PermissionUpdateDTO;
import org.dubhe.admin.domain.entity.Permission;
import org.dubhe.admin.domain.vo.PermissionVO;
import org.dubhe.admin.service.PermissionService;
import org.dubhe.admin.service.convert.PermissionConvert;
import org.dubhe.biz.base.context.UserContext;
import org.dubhe.biz.base.exception.BusinessException;
import org.dubhe.biz.base.service.UserContextService;
import org.dubhe.biz.base.utils.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description 操作权限服务实现类
 * @date 2021-04-28
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserContextService userContextService;

    @Autowired
    private PermissionConvert permissionConvert;

    @Autowired
    private AuthCodeMapper authCodeMapper;

    /**
     *
     * 获取权限列表
     * @param pid 权限父id
     * @return java.util.List 权限列表
     */
    @Override
    public List<Permission> findByPid(long pid) {
        return permissionMapper.findByPid(pid);
    }

    /**
     * 获取权限树
     *
     * @param permissions 权限列表
     * @return Object 权限树列表结构
     */
    @Override
    public Object getPermissionTree(List<Permission> permissions) {
        List<Map<String, Object>> list = new LinkedList<>();
        permissions.forEach(permission -> {
                    if (permission != null) {
                        List<Permission> authList = permissionMapper.findByPid(permission.getId());
                        Map<String, Object> map = new HashMap<>(16);
                        map.put("id", permission.getId());
                        map.put("permission", permission.getPermission());
                        map.put("label", permission.getName());
                        if (CollUtil.isNotEmpty(authList)) {
                            map.put("children", getPermissionTree(authList));
                        }
                        list.add(map);
                    }
                }
        );
        return list;
    }

    /**
     * 获取权限列表
     *
     * @param permissionQueryDTO 权限查询DTO
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> queryAll(PermissionQueryDTO permissionQueryDTO) {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(permissionQueryDTO.getKeyword())) {
            queryWrapper.and(x -> x.like(Permission::getName, permissionQueryDTO.getKeyword()).or().like(Permission::getPermission, permissionQueryDTO.getKeyword()));
        }
        queryWrapper.orderByDesc(Permission::getCreateTime);
        List<Permission> permissions = permissionMapper.selectList(queryWrapper);
        return buildTree(permissionConvert.toDto(permissions));
    }

    private Map<String, Object> buildTree(List<PermissionVO> permissions) {
        List<PermissionVO> trees = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (PermissionVO permissionVO : permissions) {
            if (permissionVO.getPid() == 0) {
                trees.add(permissionVO);
            }
            for (PermissionVO vo : permissions) {
                if (vo.getPid().equals(permissionVO.getId())) {
                    if (CollUtil.isEmpty(permissionVO.getChildren())) {
                        permissionVO.setChildren(new ArrayList<>());
                    }
                    permissionVO.getChildren().add(vo);
                    ids.add(vo.getId());
                }
            }

        }

        Map<String, Object> map = new HashMap<>(2);
        if (trees.size() == 0) {
            permissions.stream().filter(x -> !ids.contains(x.getId())).collect(Collectors.toList());
        }

        Map<String, Object> page = new HashMap<>(3);
        page.put("current", 1);
        page.put("size", permissions.size());
        page.put("total", permissions.size());

        map.put("result", trees);
        map.put("page", page);
        return map;
    }

    /**
     * 新增权限
     *
     * @param permissionCreateDTO 新增权限DTO
     */
    @Override
    public void create(PermissionCreateDTO permissionCreateDTO) {
        UserContext curUser = userContextService.getCurUser();
        List<Permission> permissions = new ArrayList<>();
        for (Permission resource : permissionCreateDTO.getPermissions()) {
            if (permissionMapper.findByName(resource.getName()) != null) {
                throw new BusinessException("权限名称已存在");
            }

            Permission permission = new Permission();
            permission.setPid(permissionCreateDTO.getPid())
                    .setName(resource.getName())
                    .setPermission(resource.getPermission())
                    .setCreateUserId(curUser.getId());
            permissions.add(permission);
        }
        saveBatch(permissions);
    }

    /**
     * 修改权限
     *
     * @param permissionUpdateDTO 修改权限DTO
     */
    @Override
    public void update(PermissionUpdateDTO permissionUpdateDTO) {
        UserContext curUser = userContextService.getCurUser();
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionUpdateDTO, permission);
        for (Permission per : permissionUpdateDTO.getPermissions()) {
            permission.setName(per.getName());
            permission.setPermission(per.getPermission());
            permission.setUpdateUserId(curUser.getId());
            permissionMapper.updateById(permission);
        }
    }

    /**
     * 删除权限
     *
     * @param permissionDeleteDTO 删除权限DTO
     */
    @Override
    public void delete(PermissionDeleteDTO permissionDeleteDTO) {
        Set<Long> ids = new HashSet<>();
        List<Permission> permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>().in(Permission::getId, permissionDeleteDTO.getIds()));
        if (CollUtil.isNotEmpty(permissions)) {
            for (Permission permission : permissions) {
                if (permission.getPid() == 0) {
                    List<Permission> permissionList = permissionMapper.findByPid(permission.getId());
                    permissionList.forEach(x -> {
                        ids.add(x.getId());
                    });
                }
                ids.add(permission.getId());
            }
        }
        //解绑权限组权限
        authCodeMapper.untiedByPermissionId(ids);
        permissionMapper.deleteBatchIds(ids);
    }
}
