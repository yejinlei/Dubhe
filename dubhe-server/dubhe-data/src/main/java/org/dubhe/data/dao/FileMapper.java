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
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.annotation.DataPermission;
import org.dubhe.constant.PermissionConstant;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.data.domain.entity.File;

import java.util.List;

/**
 * @description 文件信息 Mapper 接口
 * @date 2020-04-10
 */
@DataPermission(ignores = {"insert", "selectListByLimit", "updateSampleStatus", "getOneById", "selectPage", "selectListByLimit", "selectOne", "selectList", "selectBatchIds", "selectById", "selectCount", "update"

})
public interface FileMapper extends BaseMapper<File> {

    /**
     * 根据offset, limit查询
     *
     * @param offset       偏移量
     * @param limit        页容量
     * @param queryWrapper 查询条件
     * @return List<File> File列表
     */
    @DataPermission(permission = PermissionConstant.SELECT)
    @Select("select * from data_file ${ew.customSqlSegment} limit #{offset}, #{limit}")
    List<File> selectListByLimit(@Param("offset") long offset, @Param("limit") int limit, @Param("ew") Wrapper<File> queryWrapper);

    /**
     * 将文件状态改为采样中
     *
     * @param id     文件ID
     * @param status 文件状态
     * @return updateSampleStatus   执行次数
     */
    @Update("update data_file set status=1 where id = #{id} and status = #{status}")
    int updateSampleStatus(@Param("id") Long id, @Param("status") int status);

    /**
     * 根据文件ID获取文件
     *
     * @param fileId 文件ID
     * @return File 文件对象
     */
    @Select("select * from data_file where id = #{fileId}")
    File getOneById(@Param("fileId") Long fileId);

    /**
     * 批量保存
     *
     * @param files 上传文件列表
     * @param userId 用户Id
     * @param datasetUserId 数据集用户id
     */
    void saveList(@Param("files") List<File> files, @Param("userId") Long userId, @Param("datasetUserId") Long datasetUserId);

    /**
     * 查询图片宽高
     */
    @Select("select width,height from data_file where name = #{name} and dataset_id = #{datasetId}")
    FileCreateDTO selectWidthAndHeight(@Param("name") String name, @Param("datasetId") Long datasetId);

}
