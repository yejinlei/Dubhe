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
package org.dubhe.datasetutil.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.dubhe.datasetutil.common.base.BaseEntity;

import java.io.Serializable;

/**
 * @description nlp中间表
 * @date 2020-01-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
@TableName("data_file_annotation")
public class DataFileAnnotation extends BaseEntity implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 标签ID
     */
    private Long LabelId;

    /**
     * 数据集版本文件ID
     */
    private Long versionFileId;

    /**
     * 预测值(值=实际值*100)
     */
    private Double prediction;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 插入nlp中间表
     *
     * @param datasetId      数据集id
     * @param labelId        标签id
     * @param versionFileId  数据集版本文件id
     * @param prediction     预测值
     * @param createUserId   创建人id
     * @param fileName       文件名称
     * @return DataFileAnnotation nlp中间表
     */
    public DataFileAnnotation(Long datasetId,Long labelId,Long versionFileId,Double prediction,Long createUserId, String fileName){
        this.datasetId = datasetId;
        this.LabelId = labelId;
        this.versionFileId = versionFileId;
        this.prediction = prediction;
        this.setCreateUserId(createUserId);
        this.fileName = fileName;
    }
}
