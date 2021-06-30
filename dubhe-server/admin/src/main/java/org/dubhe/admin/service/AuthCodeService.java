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
import org.dubhe.admin.domain.dto.AuthCodeCreateDTO;
import org.dubhe.admin.domain.dto.AuthCodeQueryDTO;
import org.dubhe.admin.domain.dto.AuthCodeUpdateDTO;
import org.dubhe.admin.domain.dto.RoleAuthUpdateDTO;
import org.dubhe.admin.domain.entity.Auth;
import org.dubhe.admin.domain.vo.AuthVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 权限组服务类
 * @date 2021-05-14
 */
public interface AuthCodeService extends IService<Auth> {

    /**
     * 分页查询权限组信息
     *
     * @param authCodeQueryDTO 分页查询实例
     * @return Map<String, Object> 权限组分页信息
     */
    Map<String, Object> queryAll(AuthCodeQueryDTO authCodeQueryDTO);

    /**
     * 创建权限组
     *
     * @param authCodeCreateDTO 创建权限组DTO实例
     */
    void create(AuthCodeCreateDTO authCodeCreateDTO);

    /**
     * 修改权限组信息
     *
     * @param authCodeUpdateDTO 修改权限组信息DTO实例
     */
    void update(AuthCodeUpdateDTO authCodeUpdateDTO);

    /**
     * 批量删除权限组
     *
     * @param ids 权限组id集合
     */
    void delete(Set<Long> ids);

    /**
     * 修改角色-权限组绑定关系
     *
     * @param roleAuthUpdateDTO 角色-权限组关系映射DTO实例
     */
    void updateRoleAuth(RoleAuthUpdateDTO roleAuthUpdateDTO);

    /**
     * 获取权限组列表
     *
     * @return List<Auth> 权限组列表信息
     */
    List<AuthVO> getAuthCodeList();

}
