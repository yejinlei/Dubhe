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

package org.dubhe.data.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.dubhe.annotation.DataPermission;
import org.dubhe.constant.PermissionConstant;
import org.dubhe.data.domain.entity.Dataset;

/**
 * @description 数据集管理 Mapper 接口
 * @date 2020-04-10
 */
@DataPermission(ignores = {"insert"})
public interface DatasetMapper extends BaseMapper<Dataset> {

    /**
     * 分页获取数据集
     *
     * @param page         分页插件
     * @param queryWrapper 查询条件
     * @return Page<Dataset>数据集列表
     */
    @DataPermission(permission = PermissionConstant.SELECT)
    @Select("SELECT * FROM data_dataset ${ew.customSqlSegment}")
    @Results(id = "datasetMapperResults",
            value = {
                    @Result(column = "team_id", property = "team",
                            one = @One(select = "org.dubhe.dao.TeamMapper.selectById",
                                    fetchType = FetchType.LAZY)),
                    @Result(column = "create_user_id", property = "createUser",
                            one = @One(select = "org.dubhe.dao.UserMapper.selectById",
                                    fetchType = FetchType.LAZY)),
                    @Result(column = "update_user_id", property = "updateUser",
                            one = @One(select = "org.dubhe.dao.UserMapper.selectById",
                                    fetchType = FetchType.LAZY))
            })
    Page<Dataset> listPage(Page<Dataset> page, @Param("ew") Wrapper<Dataset> queryWrapper);

    /**
     * 修改数据集当前版本
     *
     * @param id          数据集ID
     * @param versionName 数据集版本名称
     */
    @DataPermission(permission = PermissionConstant.UPDATE)
    @Update("update data_dataset set current_version_name = #{versionName} where id = #{id}")
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
     * 更新数据集解压状态
     * @param datasetId   数据集ID
     * @param sourceState 压缩开始状态
     * @param targetState 压缩结束状态
     * @return
     */
    @Update("update data_dataset set decompress_state = #{targetState} where id = #{datasetId} and decompress_state = #{sourceState}")
    int updateDecompressState(@Param("datasetId") Long datasetId, @Param("sourceState") Integer sourceState, @Param("targetState") Integer targetState);

}
