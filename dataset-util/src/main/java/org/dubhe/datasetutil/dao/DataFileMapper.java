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
package org.dubhe.datasetutil.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.dubhe.datasetutil.domain.entity.DataFile;
import java.util.List;

/**
 * @description 数据集文件 Mapper接口
 * @date 2020-09-17
 */
public interface DataFileMapper extends BaseMapper<DataFile> {

    /**
     * 插入文件数据
     *
     * @param listDataFile 文件数据集合
     */
    void saveBatchDataFile(@Param("listDataFile") List<DataFile> listDataFile);


    /**
     * 创建新表 data_file_1
     */
    @Update("CREATE TABLE  data_file_1  LIKE data_file")
    void createNewTableOne();


    /**
     * 创建新表 data_file_2
     */
    @Update("CREATE TABLE  data_file_2  LIKE data_file")
    void createNewTableTwo();

    /**
     * 根据表名获取表数量
     *
     * @param tableName 表名称
     * @return  表数量
     */
    @Select("select count(*) from information_schema.TABLES where table_name = #{tableName}")
    int selectCountByTableName(@Param("tableName") String tableName);

    /**
     * 删除数据集文件通过数据集ID
     *
     * @param datasetId 数据集ID
     */
    @Delete("delete  from data_file where dataset_id = #{datasetId}")
    void deleteFileByDatasetId(@Param("datasetId") long datasetId);
}
