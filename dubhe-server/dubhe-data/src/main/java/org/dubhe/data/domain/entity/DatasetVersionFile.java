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

package org.dubhe.data.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.dubhe.biz.base.utils.StringUtils;
import org.dubhe.data.constant.Constant;


/**
 * @description 数据集版本文件管理
 * @date 2020-05-14
 */
@Data
@TableName("data_dataset_version_file")
@ApiModel(value = "Dataset版本文件关系表", description = "数据集版本文件管理")
@Builder
@AllArgsConstructor
public class DatasetVersionFile {

    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    @ApiModelProperty("数据集ID")
    private Long datasetId;

    @ApiModelProperty("版本名称")
    private String versionName;

    @ApiModelProperty("文件ID")
    private Long fileId;

    @ApiModelProperty("文件状态 0:新增文件 1:删除文件 2:正常文件")
    private Integer status;

    @ApiModelProperty("标注状态")
    private Integer annotationStatus;

    @ApiModelProperty("备份状态，用于版本回退")
    private Integer backupStatus;

    @ApiModelProperty("更改标记，用于版本回退")
    private Integer changed;

    @ApiModelProperty("文件名称")
    private String fileName;



    public DatasetVersionFile() {
    }

    public DatasetVersionFile(Long datasetId, String versionName, Long fileId, String fileName) {
        this.datasetId = datasetId;
        this.versionName = versionName;
        this.fileId = fileId;
        this.status = 0;
        this.changed = StringUtils.isBlank(versionName) ? Constant.UNCHANGED : Constant.CHANGED;
        this.fileName=fileName;
    }

    public DatasetVersionFile(Long datasetId, String versionName, Long fileId, Integer annotationStatus,String fileName,Integer changed) {
        this.datasetId = datasetId;
        this.versionName = versionName;
        this.fileId = fileId;
        this.status = 0;
        this.annotationStatus = annotationStatus;
        this.fileName=fileName;
        this.changed=changed;
    }

    public DatasetVersionFile(Long datasetId, String versionName, Long fileId, int status) {
        this.datasetId = datasetId;
        this.versionName = versionName;
        this.fileId = fileId;
        this.status = 0;
        this.changed = changed;
    }

    @Override
    public String toString() {
        return "DatasetVersionFile{" +
                "id=" + id +
                ", datasetId=" + datasetId +
                ", versionName='" + versionName + '\'' +
                ", fileId=" + fileId +
                ", status=" + status +
                ", annotationStatus=" + annotationStatus +
                ", backupStatus=" + backupStatus +
                ", changed=" + changed +
                '}';
    }

}
