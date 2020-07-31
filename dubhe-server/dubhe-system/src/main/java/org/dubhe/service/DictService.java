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
import org.dubhe.domain.dto.DictCreateDTO;
import org.dubhe.domain.dto.DictDTO;
import org.dubhe.domain.dto.DictQueryDTO;
import org.dubhe.domain.dto.DictUpdateDTO;
import org.dubhe.domain.entity.Dict;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description :字典服务 Service
 * @Date 2020-06-01
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
     * 查询全部数据
     *
     * @param criteria /
     * @return /
     */
    List<DictDTO> queryAll(DictQueryDTO criteria);

    /**
     * 根据ID查询
     *
     * @param id /
     * @return /
     */
    DictDTO findById(Long id);

    /**
     * 根据Name查询
     *
     * @param name /
     * @return /
     */
    DictDTO findByName(String name);

    /**
     * 创建
     *
     * @param resources /
     * @return /
     */
    DictDTO create(DictCreateDTO resources);

    /**
     * 编辑
     *
     * @param resources /
     */
    void update(DictUpdateDTO resources);

    /**
     * 多选删除
     *
     * @param ids /
     */
    void deleteAll(Set<Long> ids);

    /**
     * 导出数据
     *
     * @param queryAll 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<DictDTO> queryAll, HttpServletResponse response) throws IOException;
}
