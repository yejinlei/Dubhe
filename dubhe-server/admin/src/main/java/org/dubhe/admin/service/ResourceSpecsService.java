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
import org.dubhe.biz.base.vo.QueryResourceSpecsVO;
import org.dubhe.biz.base.dto.QueryResourceSpecsDTO;

import java.util.List;
import java.util.Map;

/**
 * @description CPU, GPU, 内存等资源规格管理
 * @date 2021-05-27
 */
public interface ResourceSpecsService {

    /**
     *  查询资源规格
     * @param resourceSpecsQueryDTO 查询资源规格请求实体
     * @return List<ResourceSpecs> resourceSpecs 资源规格列表
     */
    Map<String, Object> getResourceSpecs(ResourceSpecsQueryDTO resourceSpecsQueryDTO);

    /**
     *  新增资源规格
     * @param resourceSpecsCreateDTO  新增资源规格实体
     * @return List<Long> 新增资源规格id
     */
    List<Long> create(ResourceSpecsCreateDTO resourceSpecsCreateDTO);

    /**
     *  修改资源规格
     * @param resourceSpecsUpdateDTO  修改资源规格实体
     * @return List<Long> 修改资源规格id
     */
    List<Long> update(ResourceSpecsUpdateDTO resourceSpecsUpdateDTO);

    /**
     *  资源规格删除
     * @param resourceSpecsDeleteDTO 资源规格删除id集合
     */
    void delete(ResourceSpecsDeleteDTO resourceSpecsDeleteDTO);

    /**
     * 查询资源规格
     * @param queryResourceSpecsDTO 查询资源规格请求实体
     * @return QueryResourceSpecsVO 资源规格返回结果实体类
     */
    QueryResourceSpecsVO queryResourceSpecs(QueryResourceSpecsDTO queryResourceSpecsDTO);

    /**
     * 查询资源规格
     * @param id 资源规格id
     * @return QueryResourceSpecsVO 资源规格返回结果实体类
     */
    QueryResourceSpecsVO queryTadlResourceSpecs(Long id);
}