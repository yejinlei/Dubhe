/**
 * Copyright 2020 Zhejiang Lab. All Rights Reserved.
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

package org.dubhe.data.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dubhe.data.domain.dto.LabelGroupCopyDTO;
import org.dubhe.data.domain.dto.LabelGroupCreateDTO;
import org.dubhe.data.domain.entity.LabelGroup;
import org.dubhe.data.domain.vo.LabelGroupQueryVO;
import org.dubhe.data.domain.vo.LabelGroupVO;

import java.util.List;
import java.util.Map;

import org.dubhe.data.domain.dto.LabelGroupDeleteDTO;
import org.dubhe.data.domain.dto.LabelGroupImportDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description 标签组服务
 * @date 2020-09-22
 */
public interface LabelGroupService {

    /**
     * 创建标签组
     *
     * @param labelGroupCreateDTO 创建标签组DTO
     */
    void creatLabelGroup(LabelGroupCreateDTO labelGroupCreateDTO);

    /**
     * 更新（编辑）标签组
     *
     * @param labelGroupId        标签组ID
     * @param labelGroupCreateDTO 创建标签组DTO
     * @return Boolean 是否更新成功
     */
    void update(Long labelGroupId, LabelGroupCreateDTO labelGroupCreateDTO);

    /**
     * 删除标签组
     *
     * @param labelGroupDeleteDTO 删除标签组DTO
     */
    void delete(LabelGroupDeleteDTO labelGroupDeleteDTO);

    /**
     * 删除标签组方法
     *
     * @param labelGroupId 标签组ID
     */
    void delete(Long labelGroupId);

    /**
     * 标签组分页列表
     *
     * @param page                 分页信息
     * @param labelGroupQueryVO    查询条件
     * @return Map<String,Object>  查询出对应的标签组
     */
    Map<String, Object> listVO(Page<LabelGroup> page, LabelGroupQueryVO labelGroupQueryVO);

    /**
     * 标签组详情
     *
     * @param labelGroupId   标签组id
     * @return LabelGroupVO  根据Id查询出对应的标签组
     */
    LabelGroupVO get(Long labelGroupId);

    /**
     * 标签组列表
     *
     * @param type 标签组类型
     * @return List<LabelGroup> 查询出对应的标签组
     */
    List<LabelGroup> getList(Integer type);

    /**
     * 导入标签组
     *
     * @param labelGroupImportDTO 标签组导入DTO
     * @param file                导入文件
     */
    void importLabelGroup(LabelGroupImportDTO labelGroupImportDTO, MultipartFile file);

    /**
     * 标签组复制
     *
     * @param labelGroupCopyDTO 标签组复制DTO
     */
    void copy(LabelGroupCopyDTO labelGroupCopyDTO);
}
