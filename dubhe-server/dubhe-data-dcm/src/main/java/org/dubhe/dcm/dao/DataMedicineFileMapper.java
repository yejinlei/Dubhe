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
package org.dubhe.dcm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.dcm.domain.entity.DataMedicineFile;

import java.util.List;
import java.util.Map;

/**
 * @description 医学数据集文件管理 Mapper 接口
 * @date 2020-11-12
 */
@DataPermission(ignoresMethod = {"insert","saveBatch"})
public interface DataMedicineFileMapper extends BaseMapper<DataMedicineFile> {

    /**
     * 根据医学数据集ID获取文件url列表
     *
     * @param medicineId 数据集ID
     * @return List<String>  文件url列表
     */
    @Select("select url from data_medicine_file where medicine_id = #{medicineId} and deleted = 0")
    List<String> listMedicineFilesUrls(@Param("medicineId")Long medicineId);






    /**
     * 查询文件状态取并集
     *
     * @param medicalIds 数据集ID
     * @return 文件状态列表
     */
    List<Map<String,Object>> getFileStatusCount(@Param("medicalIds")List<Long> medicalIds);

    /**
     * 批量更新 文件状态
     *
     * @param fileIds 文件ID集合
     * @param status  文件状态
     * @return int    更新数量
     */
    @Update("<script>"
            + " update data_medicine_file  set status = #{status} where id in "
            + " <foreach collection='fileIds' item='id' open='(' separator=',' close=')'> "
            + " #{id}"
            + " </foreach>"
            + "</script>")
    int updateStatusByIds(@Param("fileIds") List<Long> fileIds, @Param("status")  Integer status);

    /**
     * 查询文件集合
     *
     * @param fileIds 文件ID集合
     * @return List<DataMedicineFile> 文件集合
     */
    @Select("<script>"
            + " select * from data_medicine_file where  deleted = 0 and id in "
            + " <foreach collection='fileIds' item='id' open='(' separator=',' close=')'> "
            + " #{id}"
            + " </foreach>"
            + "</script>")
    List<DataMedicineFile> selectByIds(@Param("fileIds")List<Long> fileIds);


    /**
     * 批量插入医学数据集文件中间表
     *
     * @param dataMedicineFiles 医学数据集文件中间表数据
     */
    void saveBatch(@Param("dataMedicineFiles") List<DataMedicineFile> dataMedicineFiles);

    /**
     * 更新修改人ID
     *
     * @param medicineId 医学数据集id
     * @param userId     当前操作人id
     */
    @Update("update data_medicine_file set update_user_id = #{userId} where medicine_id = #{medicineId}")
    void updateUserIdByMedicineId(@Param("medicineId") Long medicineId, @Param("userId") Long userId);


    /**
     * 更新数据集删除状态
     *
     * @param id            数据集ID
     * @param deleteFlag    数据集状态
     */
    @Update("update data_medicine_file set deleted = #{deleteFlag} where id = #{id}")
    void updateStatusById(@Param("id") Long id, @Param("deleteFlag") Boolean deleteFlag);


    /**
     * 根据医学数据集id删除医学数据集文件数据
     *
     * @param id 医学数据集ID
     */
    @Delete("DELETE FROM data_medicine_file WHERE medicine_id= #{id} ")
    void deleteByDatasetId(@Param("id") Long id);
}
