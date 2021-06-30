/**
 * Copyright 2020 Tianshu AI Platform. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================
 */
package org.dubhe.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.admin.domain.entity.DataSequence;

/**
 * @description 数据序列 Mapper
 * @date 2020-09-23
 */
public interface DataSequenceMapper extends BaseMapper<DataSequence> {
    /**
     * 根据业务编码查询序列
     * @param businessCode  业务编码
     * @return DataSequence 序号
     */
    @Select("select id, business_code ,start, step from data_sequence where business_code = #{businessCode} for update")
    DataSequence selectByBusiness(String businessCode);

    /**
     * 根据业务编码更新序列起始值
     * @param businessCode  业务编码
     * @return DataSequence 序号
     */
    @Update("update data_sequence set start = start + step where business_code = #{businessCode} ")
    int updateStartByBusinessCode(String businessCode);

    /**
     * 查询存在表的记录数
     * @param tableName 表名
     * @return int 查询表记录数
     */
    @Select("select count(1) from ${tableName}")
    int checkTableExist(@Param("tableName") String tableName);

    /**
     * 执行创建表
     * @param tableName    新表表名
     * @param oldTableName 模板表表名
     */
    @Update({"CREATE TABLE ${tableName} like ${oldTableName}"})
    void createNewTable(@Param("tableName") String tableName, @Param("oldTableName") String oldTableName);

}
