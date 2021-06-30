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
import org.dubhe.admin.domain.dto.DictCreateDTO;
import org.dubhe.admin.domain.dto.DictDTO;
import org.dubhe.admin.domain.dto.DictQueryDTO;
import org.dubhe.admin.domain.dto.DictUpdateDTO;
import org.dubhe.admin.domain.entity.Dict;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 字典服务 Service
 * @date 2020-06-01
 */
public interface DictService {

    /**
     * 分页查询
     *
     * @param criteria 条件
     * @param page     分页参数
     * @return /
     */
    Map<String, Object> queryAll(DictQueryDTO criteria, Page<Dict> page);

    /**
     * 按条件查询字典列表
     *
     * @param criteria 字典查询实体
     * @return java.util.List<org.dubhe.domain.dto.DictDTO> 字典实例
     */
    List<DictDTO> queryAll(DictQueryDTO criteria);

    /**
     * 通过ID查询字典详情
     *
     * @param id 字典ID
     * @return org.dubhe.domain.dto.DictDTO 字典实例
     */
    DictDTO findById(Long id);

    /**
     * 通过Name查询字典详情
     *
     * @param name 字典名称
     * @return org.dubhe.domain.dto.DictDTO 字典实例
     */
    DictDTO findByName(String name);

    /**
     * 新增字典
     *
     * @param resources 字典新增实体
     * @return org.dubhe.domain.dto.DictDTO 字典实例
     */
    DictDTO create(DictCreateDTO resources);

    /**
     * 字典修改
     *
     * @param resources 字典修改实体
     */
    void update(DictUpdateDTO resources);

    /**
     * 字典批量删除
     *
     * @param ids 字典ID
     */
    void deleteAll(Set<Long> ids);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response 导出http响应
     * @throws IOException 导出异常
     */
    void download(List<DictDTO> queryAll, HttpServletResponse response) throws IOException;
}
