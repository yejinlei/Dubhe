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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.admin.domain.dto.*;
import org.dubhe.admin.domain.entity.DictDetail;
import org.dubhe.biz.base.dto.DictDetailQueryByLabelNameDTO;
import org.dubhe.biz.base.vo.DictDetailVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 字典详情服务 Service
 * @date 2020-06-01
 */
public interface DictDetailService {

    /**
     * 根据ID查询
     *
     * @param id 字典Id
     * @return 字典详情DTO
     */
    DictDetailDTO findById(Long id);

    /**
     * 创建
     *
     * @param resources 字典详情创建实体
     * @return 字典详情实体
     */
    DictDetailDTO create(DictDetailCreateDTO resources);

    /**
     * 编辑
     *
     * @param resources 字典详情修改实体
     */
    void update(DictDetailUpdateDTO resources);

    /**
     * 删除
     *
     * @param ids 字典详情ids
     */
    void delete(Set<Long> ids);

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param page     分页参数
     * @return 字典分页列表数据
     */
    Map<String, Object> queryAll(DictDetailQueryDTO criteria, Page<DictDetail> page);

    /**
     * 查询全部数据
     *
     * @param criteria 字典查询实体
     * @return 字典列表数据
     */
    List<DictDetailDTO> queryAll(DictDetailQueryDTO criteria);

    /**
     * 根据名称查询字典详情
     *
     * @param dictDetailQueryByLabelNameDTO 字典名称
     * @return List<DictDetail> 字典集合
     */
    List<DictDetailVO> getDictName(DictDetailQueryByLabelNameDTO dictDetailQueryByLabelNameDTO);
}
