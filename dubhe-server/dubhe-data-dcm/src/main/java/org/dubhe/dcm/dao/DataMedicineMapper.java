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
import org.dubhe.dcm.domain.entity.DataMedicine;

/**
 * @description 医学数据集管理 Mapper 接口
 * @date 2020-11-11
 */
@DataPermission(ignoresMethod = {"insert"})
public interface DataMedicineMapper extends BaseMapper<DataMedicine> {

    /**
     * 根据序列实例UID查询医学数据集
     *
     * @param seriesUid 序列实例UID
     * @param originUserId 资源拥有人
     * @return DataMedicine 医学数据集
     */
    @Select("select * from data_medicine where series_instance_uid = #{seriesUid} and origin_user_id = #{originUserId} and deleted = 0")
    DataMedicine findBySeriesUidAndNotId(@Param("seriesUid") String seriesUid, @Param("originUserId") Long originUserId);


    /**
     * 更新数据集状态
     *
     * @param id        数据集ID
     * @param status    数据集状态
     */
    @Update("update data_medicine set status = #{status} where id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);


    /**
     * 更新数据集删除状态
     *
     * @param id            数据集ID
     * @param deleteFlag    数据集状态
     */
    @Update("update data_medicine set deleted = #{deleteFlag} where id = #{id}")
    void updateStatusById(@Param("id") Long id, @Param("deleteFlag") Boolean deleteFlag);

    /**
     * 根据id删除数据集
     *
     * @param id 医学数据集id
     */
    @Delete("DELETE FROM data_medicine WHERE id= #{id} ")
    void deleteByDatasetId(@Param("id")Long id);

    /**
     * 根据序列实例UID查询医学数据集
     *
     * @param seriesInstanceUid 序列实例UID
     * @return DataMedicine 医学数据集
     */
    @Select("select * from data_medicine where series_instance_uid = #{seriesUid}  and deleted = 0")
    DataMedicine findDataMedicineBySeriesUid(@Param("seriesUid") String seriesInstanceUid);

    /**
     * 根据医学数据集ID查询被回收的医学数据集
     *
     * @param datasetId     医学数据集ID
     * @return DataMedicine 医学数据集
     */
    @Select("select * from data_medicine where id = #{id}  and deleted = 1")
    DataMedicine findDataMedicineByIdAndDeleteIsFalse(@Param("id") Long datasetId);
}
