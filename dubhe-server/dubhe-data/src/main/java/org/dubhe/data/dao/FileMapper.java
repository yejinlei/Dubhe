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
import org.apache.ibatis.annotations.Update;
import org.dubhe.biz.base.annotation.DataPermission;
import org.dubhe.data.domain.dto.EsTransportDTO;
import org.dubhe.data.domain.dto.FileCreateDTO;
import org.dubhe.data.domain.entity.File;

import java.util.List;

/**
 * @description 文件信息 Mapper 接口
 * @date 2020-04-10
 */
@DataPermission(ignoresMethod = {"insert", "getOneById", "selectFile"})
public interface FileMapper extends BaseMapper<File> {


    /**
     * 根据文件ID获取文件
     *
     * @param fileId    文件ID
     * @param datasetId 数据集ID
     * @return File     文件对象
     */
    @Select("select * from data_file where id = #{fileId} and dataset_id = #{datasetId}")
    File getOneById(@Param("fileId") Long fileId, @Param("datasetId") long datasetId);

    /**
     * 批量保存
     *
     * @param files         上传文件列表
     * @param userId        用户Id
     * @param datasetUserId 数据集用户id
     */
    void saveList(@Param("files") List<File> files, @Param("userId") Long userId, @Param("datasetUserId") Long datasetUserId);

    /**
     * 查询图片宽高
     *
     * @param name              数据集的版本文件名称
     * @param datasetId         数据集ID
     * @return FileCreateDTO    文件详情
     */
    @Select("select width,height from data_file where name = #{name} and dataset_id = #{datasetId}")
    FileCreateDTO selectWidthAndHeight(@Param("name") String name, @Param("datasetId") Long datasetId);

    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @param datasetId 数据集ID
     * @return File 文件详情
     */
    @Select("select * from data_file where id = #{fileId} and dataset_id=#{datasetId} and deleted=0")
    File selectFile(@Param("fileId") Long fileId, @Param("datasetId") Long datasetId);

    /**
     * 分页获取数据集文件
     *
     * @param datasetId          数据集ID
     * @param currentVersionName 数据集版本名称
     * @param offset             偏移量
     * @param batchSize          批长度
     * @return List 文件列表
     */
    @Select("<script>" +
            "select distinct df.* from data_dataset_version_file ddvf left join data_file df on ddvf.file_id = df.id where ddvf.dataset_id = #{datasetId} " +
            " and df.dataset_id = #{datasetId} " +
            "<if test='currentVersionName != null'> " +
            "and ddvf.version_name =  #{currentVersionName} " +
            "</if>" +
            "and ddvf.annotation_status = 101 " +
            "limit #{offset}, #{batchSize} " +
            "</script>")
    List<File> selectListOne(@Param("datasetId") Long datasetId, @Param("currentVersionName") String currentVersionName, @Param("offset") int offset, @Param("batchSize") int batchSize);

    /**
     * 更新文件状态
     *
     * @param datasetId  数据集ID
     * @param id         文件ID
     * @param status     文件状态
     */
    @Update("update data_file set status = #{status} where dataset_id = #{datasetId} and id = #{id}")
    void updateFileStatus(@Param("datasetId") Long datasetId, @Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据数据集ID删除文件数据
     *
     * @param datasetId     数据集ID
     * @param limitNumber   删除数量
     * @return int 成功删除条数
     */
    @Delete("delete from data_file where dataset_id = #{datasetId} limit #{limitNumber} ")
    int deleteByDatasetId(@Param("datasetId") Long datasetId, @Param("limitNumber") int limitNumber);

    /**
     * 根据版本和数据集ID获取文件url
     *
     * @param datasetId     数据集ID
     * @param versionName   版本名
     * @return List<String> url列表
     */
    @Select("select df.url from data_file df left join data_dataset_version_file ddvf on df.id = ddvf.file_id " +
            "where ddvf.dataset_id = #{datasetId} and df.dataset_id = #{datasetId} " +
            "and ddvf.version_name = #{versionName}")
    List<String> selectUrls(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 根据version.changed获取文件name列表
     *
     * @param datasetId     数据集ID
     * @param changed       版本文件是否改动
     * @param versionName   版本名称
     * @return List<name>   名称列表
     */
    @Select("select df.name from data_file df left join data_dataset_version_file ddvf on df.id = ddvf.file_id " +
            "where ddvf.dataset_id = #{datasetId} and df.dataset_id = #{datasetId} " +
            "and ddvf.changed = #{changed} and ddvf.version_name = #{versionName}")
    List<String> selectNames(@Param("datasetId") Long datasetId, @Param("changed") Integer changed,@Param("versionName")String versionName);

    /**
     * 获取当前版本下原图文件数量
     *
     * @param datasetId 数据集ID
     * @param versionName 版本名称
     * @return 数据集文件数量
     */
    int getOriginalFileCountOfDataset(@Param("datasetId") Long datasetId, @Param("versionName") String versionName);

    /**
     * 批量新增文件数据
     *
     * @param fileList 数据文件列表
     */
    void insertBatch(@Param("listDataFile")List<File> fileList);

    /**
     * 选择需要同步到ES的数据
     *
     * @param datasetId 数据集ID
     * @param fileIdsNotToEs 需要同步的文件ID
     * @return List<EsTransportDTO>  ES数据同步DTO
     */
    List<EsTransportDTO> selectTextDataNoTransport(@Param("datasetId") Long datasetId,@Param("fileIdsNotToEs")List<Long> fileIdsNotToEs,
                                                   @Param("ifImport") Boolean ifImport);

    /**
     * 更新同步es标志
     *
     * @param datasetId  数据集ID
     * @param fileIds    文件ID列表
     */
    void updateEsStatus(@Param("datasetId") Long datasetId,@Param("fileIds")List<Long> fileIds);

    /**
     * 置回es标志
     *
     * @param datasetId  数据集ID
     * @param fileId     文件ID
     */
    @Update("update data_file set es_transport = 0 where dataset_id = #{datasetId} and id = #{fileId}")
    void recoverEsStatus(@Param("datasetId") Long datasetId, @Param("fileId") Long fileId);
}
