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

package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dubhe.data.domain.dto.LabelDTO;
import org.dubhe.data.domain.entity.Label;

import java.util.List;

/**
 * @description 数据集标签管理 Mapper 接口
 * @date 2020-04-10
 */
public interface LabelMapper extends BaseMapper<Label> {

    /**
     * 查询数据集下所有标签
     *
     * @param datasetId 数据集ID
     * @return List<Label> 数据集下所有标签
     */
    @Select("select dl.id, dl.name, dl.color, dl.type from data_label dl left join data_dataset_label ddl on dl.id = ddl.label_id where ddl.dataset_id = #{datasetId}  and dl.deleted = 0")
    List<Label> listLabelByDatasetId(@Param("datasetId") Long datasetId);

    /**
     * 获取数据集下所有标签类型
     *
     * @param datasetId 数据集ID
     * @return List<Integer> 数据集下所有标签类型
     */
    @Select("select distinct dl.type from data_label dl left join data_dataset_label ddl on dl.id = ddl.label_id where ddl.dataset_id = #{datasetId} and dl.deleted = 0")
    List<Integer> getDatasetLabelTypes(@Param("datasetId") Long datasetId);

    /**
     * 根据标签类型获取标签
     *
     * @param type 标签类型
     * @return List<Label> 标签类型获取标签
     */
    @Select("select * from data_label where `type` = #{type}  and deleted = 0")
    List<Label> selectListByType(@Param("type") Integer type);

    /**
     * 搜索
     *
     * @param datasetId 数据集ID
     * @return List<Label> 搜索标签信息
     */
    List<Label> batchListByIds(@Param("datasetId") Long datasetId);


    /**
     * 根据标签组获取标签列表
     *
     * @param labelGroupId 标签组ID
     * @return List<Label> 标签组列表
     */
    @Select("select dl.id, dl.name, dl.color, dl.type from data_label dl left join data_group_label dgl on dl.id = dgl.label_id\n" +
            "where dgl.label_group_id = #{labelGroupId} and dl.deleted = 0")
    List<Label> listByGroupId(@Param("labelGroupId") Long labelGroupId);

    /**
     * 根据标签组获取标签列表
     *
     * @param type 标签组类型
     * @return List<Long> 标签ids
     */
    @Select("select dl.id from data_label_group dlg left join data_group_label dgl on dlg.id = dgl.label_group_id\n" +
            "left join data_label dl on dgl.label_id = dl.id where dlg.type = #{type}")
    List<Long> listPubLabelByType(@Param("type") Integer type);




    /**
     * 根据数据集ID获取数据集对应标签组下的标签列表
     *
     * @param datasetId 数据集ID
     * @return List<LabelDTO> 标签列表
     */
    @Select("select dl.*,dd.label_group_id from data_label dl\n" +
            "left join data_group_label dgl on dl.id = dgl.label_id\n" +
            "left join data_dataset dd on dgl.label_group_id = dd.label_group_id\n" +
            "where dd.id = #{datasetId}  and dl.deleted = 0")
    List<LabelDTO> listByDatesetId(@Param("datasetId") Long datasetId);


    /**
     * 通过标签ID修改标签状态
     *
     * @param labelIds   标签ID
     * @param deleteFlag 删除标识
     */
    void updateStatusByLabelIds(@Param("labelIds") List<Long> labelIds, @Param("deleteFlag") Boolean deleteFlag);

    /**
     * 根据标签组ID删除标签数据
     *
     * @param groupId  标签组ID
     */
    @Delete("delete from data_label where id in ( select * from (select label_id from data_group_label where label_group_id = #{groupId}) m )")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
