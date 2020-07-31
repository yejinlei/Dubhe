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
package org.dubhe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.domain.dto.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @Description :角色服务 Service
 * @Date 2020-06-01
 */
public interface RoleService {

    /**
     * 根据ID查询
     *
     * @param id /
     * @return /
     */
    RoleDTO findById(long id);

    /**
     * 创建
     *
     * @param resources /
     * @return /
     */
    RoleDTO create(RoleCreateDTO resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(RoleUpdateDTO resources);

    /**
     * 删除
     *
     * @param ids /
     */
    void delete(Set<Long> ids);


    /**
     * 修改绑定的菜单
     *
     * @param resources /
     * @param roleDTO   /
     */
    void updateMenu(RoleUpdateDTO resources, RoleDTO roleDTO);

    /**
     * 解绑菜单
     *
     * @param id /
     */
    void untiedMenu(Long id);
    
    /**
     * 查询全部角色
     *
     * @param criteria 条件
     * @return /
     */
    List<RoleSmallDTO> queryAllSmall(RoleQueryDTO criteria);

    /**
     * 待条件分页查询
     *
     * @param criteria 条件
     * @param page     分页参数
     * @return /
     */
    Object queryAll(RoleQueryDTO criteria, Page page);

    /**
     * 查询全部
     *
     * @param criteria 条件
     * @return /
     */
    List<RoleDTO> queryAll(RoleQueryDTO criteria);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<RoleDTO> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 查找用户角色
     *
     * @param userId
     * @return
     */
    List<RoleSmallDTO> getRoleByUserId( Long userId);

    /**
     * 查找用户角色
     *
     * @param teamId
     * @param userId
     * @return
     */
    List<RoleSmallDTO> getRoleByUserIdAndTeamId( Long userId,Long teamId);


//
}
