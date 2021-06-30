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

package org.dubhe.data.service;

import org.dubhe.data.domain.dto.LabelDTO;
import org.dubhe.data.domain.dto.LabelDeleteDTO;
import org.dubhe.data.domain.dto.LabelUpdateDTO;
import org.dubhe.data.domain.entity.Label;

import java.util.List;

/**
 * @description 标签服务
 * @date 2020-04-10
 */
public interface LabelService {

    /**
     * 根据数据ID获取包含标签列表
     *
     * @param datasetId 数据ID
     * @return List<LabelDTO> 根据数据ID获取包含标签列表
     */
    List<LabelDTO> list(Long datasetId);

    /**
     * 根据类型获取预置标签集合
     *
     * @param labelGroupType 标签组类型
     * @return List<Label> 预置标签集合
     */
    List<Label> listSupportAutoByType(Integer labelGroupType);

    /**
     * 保存标签
     *
     * @param label        标签
     * @param datasetId    数据集ID
     * @return             标签ID
     */
    Long save(Label label, Long datasetId);

    /**
     * 保存标签
     *
     * @param labels    标签
     * @param datasetId 数据集id
     */
    void save(List<Label> labels, Long datasetId);

    /**
     * 获取指定类型下所有标签
     *
     * @param type          标签类型
     * @return List<Label>  指定类型下所有标签
     */
    List<Label> listByType(Integer type);

    /**
     * 获取数据集下所有标签类型
     *
     * @param datasetId         数据集id
     * @return List<Integer>    数据集下所有标签类型
     */
    List<Integer> getDatasetLabelTypes(Long datasetId);

    /**
     * 删除数据集标签
     *
     * @param id         数据集id
     * @param deleteFlag 删除标识
     */
    void updateStatusByDatasetId(Long id,Boolean deleteFlag);


    /**
     * 批量删除标签
     *
     * @param ids 需删除的标签ID
     */
    void deleteByIds(List<Long> ids);

    /**
     * 修改标签
     *
     * @param labelUpdateDTO 修改标签DTO
     * @return boolean      修改结果是否成功
     */
    boolean update(LabelUpdateDTO labelUpdateDTO);


    /**
     * 新增标签
     *
     * @param label 标签实体
     * @return  新增标签结果
     */
    int insert(Label label);

    /**
     * 根据标签组获取标签列表
     *
     * @param labelGroupId 标签组ID
     * @return List<Label> 标签组列表
     */
    List<Label> listByGroupId(Long labelGroupId);


    /**
     * 根据标签组类型获取标签列表
     *
     * @param type 标签组ID
     * @return List<Long> 标签IDS
     */
    List<Long> listPubLabelByType(Integer type);

    /**
     * 编辑标签
     *
     * @param label 标签实体
     */
    void updateLabel(Label label);

    /**
     * 获取预置标签组下的标签id
     *
     * @param labelGroupType 标签组类型
     * @return 预置标签ids
     */
    List<Long> getPubLabelIds(Integer labelGroupType);

    /**
     * 获取预置标签组下的标签
     *
     * @param labelGroupType 标签组类型
     * @return 预置标签集合
     */
    List<Label> getPubLabels(Integer labelGroupType);

    /**
     * 获取标签数量
     *
     * @param id 标签组Id
     * @return  标签数量
     */
    int selectCount(Long id);

    /**
     * 根据标签组类型获取标签列表
     *
     * @param labelGroupType 标签组类型
     * @return List<Label> 标签组列表
     */
    List<Label> findByLabelGroupType(Integer labelGroupType);

    /**
     * 校验数据集下标签名称是否重复
     *
     * @param datasetId 数据集ID
     * @param labelName 标签名称
     * @return 是否重复
     */
    boolean checkoutLabelIsRepeat(Long datasetId,String labelName);

    /**
     * 删除标签
     *
     * @param labelDeleteDTO 需要删除的标签DTO
     */
    void delete(LabelDeleteDTO labelDeleteDTO);

    /**
     * 通过标签ID修改标签状态
     *
     * @param labelIds   标签ID
     * @param deleteFlag 删除标识
     */
    void updateStatusByLabelIds(List<Long> labelIds, Boolean deleteFlag);

    /**
     * 根据标签组ID删除标签数据
     *
     * @param groupId  标签组ID
     */
    void deleteByGroupId(Long groupId);

    /**
     * 根据标签组ID修改状态
     *
     * @param groupId 标签组ID
     * @param deletedFlag 删除标识
     */
    void updateStatusByGroupId(Long groupId, Boolean deletedFlag);
}
