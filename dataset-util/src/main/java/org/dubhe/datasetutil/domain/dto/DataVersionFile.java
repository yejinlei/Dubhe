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
package org.dubhe.datasetutil.domain.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.dubhe.datasetutil.common.base.BaseEntity;

import java.io.Serializable;


/**
 * @description 数据集文件关系类
 * @date 2020-9-17
 */
@Data
public class DataVersionFile extends BaseEntity implements Serializable  {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据集id
     */
    private Long datasetId;

    /**
     * 版本号
     */
    private String versionName;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 数据集状态
     */
    private Integer annotationStatus;

    /**
     * 数据集状态备份
     */
    private Integer backupStatus;

    /**
     * 发布是否转换
     */
    private Integer changed;

    public DataVersionFile() {
    }

    /**
     * 插入数据集版本文件关系
     *
     * @param datasetId         数据集id
     * @param fileId            文件id
     * @param annotationStatus  数据集id
     * @param status            状态
     * @return DataVersionFile  数据集版本文件表
     */       
    public DataVersionFile(Long datasetId, Long fileId,Integer annotationStatus,Integer status) {
        this.datasetId = datasetId;
        this.fileId = fileId;
        this.annotationStatus = annotationStatus;
        this.status = status;
    }
}
