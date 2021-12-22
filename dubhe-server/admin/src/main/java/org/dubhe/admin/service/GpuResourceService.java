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

import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.GpuResource;
import org.dubhe.admin.domain.vo.GpuResourceQueryVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description GPU资源管理
 * @date 2021-08-20
 */
public interface GpuResourceService {

    /**
     *  查询GPU资源
     * @param gpuResourceQueryDTO 查询GPU资源请求实体
     * @return List<GpuResource> gpuResourceSpecs GPU资源列表
     */
    Map<String, Object> getGpuResource(GpuResourceQueryDTO gpuResourceQueryDTO);

    /**
     *  新增GPU资源
     * @param gpuResourceCreateDTO  新增GPU资源实体
     * @return List<Long> 新增GPU资源id
     */
    List<Long> create(GpuResourceCreateDTO gpuResourceCreateDTO);

    /**
     *  修改GPU资源
     * @param gpuResourceUpdateDTO  修改GPU资源实体
     * @return List<Long> 修改GPU资源id
     */
    List<Long> update(GpuResourceUpdateDTO gpuResourceUpdateDTO);

    /**
     *  GPU资源删除
     * @param gpuResourceDeleteDTO GPU资源删除id集合
     */
    void delete(GpuResourceDeleteDTO gpuResourceDeleteDTO);

    /**
     *  查询GPU类型
     * @return List<string>  GPU类型列表
     */
    List<String> getGpuType();

    /**
     *  查询用户GPU类型
     * @return Set<string>  GPU类型列表
     */
    Set<String> getUserGpuType();

    /**
     * 根据用户GPU类型查询用户GPU资源
     * @return List<GpuResource>  用户GPU资源列表
     */
    List<GpuResource> getUserGpuResource(UserGpuResourceQueryDTO userGpuResourceQueryDTO);
}