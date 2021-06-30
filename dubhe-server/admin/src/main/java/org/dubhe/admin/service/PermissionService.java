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
package org.dubhe.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.dubhe.admin.domain.dto.PermissionCreateDTO;
import org.dubhe.admin.domain.dto.PermissionDeleteDTO;
import org.dubhe.admin.domain.dto.PermissionQueryDTO;
import org.dubhe.admin.domain.dto.PermissionUpdateDTO;
import org.dubhe.admin.domain.entity.Permission;

import java.util.List;
import java.util.Map;

/**
 * @description 操作权限服务
 * @date 2021-04-28
 */
public interface PermissionService extends IService<Permission> {


    /**
     * 根据ID获取权限列表
     *
     * @param pid id
     * @return java.util.List<org.dubhe.domain.entity.Permission> 权限返回列表
     */
    List<Permission> findByPid(long pid);

    /**
     * 获取权限树
     *
     * @param permissions 权限列表
     * @return java.lang.Object 权限树树结构列表
     */
    Object getPermissionTree(List<Permission> permissions);


    /**
     * 获取权限列表
     *
     * @param permissionQueryDTO 权限查询DTO
     * @return Map<String, Object>
     */
    Map<String, Object> queryAll(PermissionQueryDTO permissionQueryDTO);

    /**
     * 新增权限
     *
     * @param permissionCreateDTO 新增权限DTO
     */
    void create(PermissionCreateDTO permissionCreateDTO);

    /**
    * 修改权限
    *
    * @param permissionUpdateDTO 修改权限DTO
    */
    void update(PermissionUpdateDTO permissionUpdateDTO);

    /**
     * 删除权限
     *
     * @param permissionDeleteDTO 删除权限DTO
     */
    void delete(PermissionDeleteDTO permissionDeleteDTO);


}
