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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.data.domain.entity.Dataset;

/**
 * @description 数据集管理 Mapper 接口
 * @date 2020-04-10
 */
@DataPermission(ignoresMethod = {"insert", "selectById", "selectCountByPublic", "selectList"})
public interface DatasetMapper extends BaseMapper<Dataset> {

    /**
     * 分页获取数据集
     *
     * @param page          分页插件
     * @param queryWrapper  查询条件
     * @return Page<Dataset>数据集列表
     */
    @Select("SELECT * FROM data_dataset ${ew.customSqlSegment}")
    Page<Dataset> listPage(Page<Dataset> page, @Param("ew") Wrapper<Dataset> queryWrapper);

    /**
     * 修改数据集当前版本
     *
     * @param id          数据集ID
     * @param versionName 数据集版本名称
     */
    @Update("update data_dataset set current_version_name = #{versionName}  where id = #{id}")
    void updateVersionName(@Param("id") Long id, @Param("versionName") String versionName);

    /**
     * 更新数据集状态
     *
     * @param datasetId 数据集ID
     * @param status    数据集状态
     */
    @Update("update data_dataset set status = #{status} where id = #{datasetId}")
    void updateStatus(@Param("datasetId") Long datasetId, @Param("status") Integer status);


    /**
     * 获取指定类型数据集的数量
     *
     * @param type 数据集类型
     * @return int  公共数据集的数量
     */
    @Select("SELECT count(1) FROM data_dataset where type = #{type} and deleted = #{deleted}")
    int selectCountByPublic(@Param("type") Integer type,@Param("deleted") Integer deleted);


    /**
     * 根据标签组ID查询关联的数据集数量
     *
     * @param labelGroupId 标签组ID
     * @return int 数量
     */
    @Select("SELECT count(1) FROM data_dataset where label_group_id = #{labelGroupId}")
    int getCountByLabelGroupId(@Param("labelGroupId") Long labelGroupId);

    /**
     * 数据集数据删除
     *
     * @param id            数据集id
     * @param deleteFlag    删除标识
     * @return int 数量
     */
    @Update("update data_dataset set deleted = #{deleteFlag} where id = #{id}")
    int updateStatusById(@Param("id") Long id, @Param("deleteFlag") boolean deleteFlag);

    /**
     * 根据数据集ID删除数据信息
     *
     * @param datasetId 数据集ID
     */
    @Delete("delete from data_dataset where  id = #{datasetId}")
    void deleteInfoById(@Param("datasetId") Long datasetId);

}
