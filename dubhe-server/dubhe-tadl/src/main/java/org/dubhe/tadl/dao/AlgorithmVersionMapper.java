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
package org.dubhe.tadl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.tadl.domain.entity.AlgorithmVersion;

import java.sql.Timestamp;

/**
 * @description 算法版本管理服务Mapper
 * @date 2021-03-22
 */
public interface AlgorithmVersionMapper extends BaseMapper<AlgorithmVersion>{

    /**
     * 获取指定算法当前使用最大版本号
     *
     * @param algorithmId     数据集ID
     * @return String         指定算法当前使用最大版本号
     */
    @Select("select max(version_name) from tadl_algorithm_version where algorithm_id=#{algorithmId} and version_name like 'V%'")
    String getMaxVersionName(@Param("algorithmId") Long algorithmId);

    @Update("update tadl_algorithm_version set deleted = #{deleted} where id = #{id}")
    int updateAlgorithmVersionStatus(@Param("id") Long id,@Param("deleted") Boolean deleted);

}
