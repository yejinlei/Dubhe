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
import io.lettuce.core.dynamic.annotation.Param;
import org.dubhe.data.domain.entity.DataFileAnnotation;

import java.util.List;

/**
 * @description 数据文件标注服务Mapper
 * @date 2021-01-06
 */
public interface DataFileAnnotationMapper  extends BaseMapper<DataFileAnnotation> {

    /**
     * 批量删除数据标注信息
     *
     * @param ids 数据集文件标注ID列表
     */
    void deleteBatch(List<Long> ids);


    /**
     * 根据版本ID查询标签列表
     *
     * @param versionFileId 版本ID
     * @return  标签ID列表
     */
    List<Long> findInfoByVersionId(@Param("versionFileId") Long versionFileId);

    /**
     * 批量保存数据文件标注信息
     *
     * @param dataFileAnnotations   数据文件标注实体
     */
    void insertBatch(List<DataFileAnnotation> dataFileAnnotations);

    /**
     * 批量修改标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     * @param fileLabelIds  标注标签Ids
     * @param prediction    预测值
     */
    void updateAnnotationFileByVersionIdAndLabelIds(@Param("versionFileId") Long versionFileId,
                                                    @Param("fileLabelIds") List<Long> fileLabelIds,@Param("prediction") Double prediction);

    /**
     * 批量删除标注文件数据
     *
     * @param versionFileId 版本文件ID
     * @param fileLabelIds  标注标签Ids
     */
    void deleteAnnotationFileByVersionIdAndLabelIds(@Param("versionFileId")Long versionFileId, @Param("fileLabelIds") List<Long> fileLabelIds);
}
