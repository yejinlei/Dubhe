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

package org.dubhe.data.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;

import java.sql.Timestamp;

/**
 * @description 数据集版本管理
 * @date 2020-05-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("data_dataset_version")
@ApiModel(value = "Dataset版本对象", description = "数据集版本管理")
public class DatasetVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "所属数据集ID")
    private Long datasetId;

    @ApiModelProperty(value = "团队ID")
    private Long teamId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Timestamp createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Timestamp updateTime;

    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    @ApiModelProperty(value = "版本号")
    private String versionName;

    @ApiModelProperty(value = "版本说明")
    private String versionNote;

    @ApiModelProperty(value = "来源版本号")
    private String versionSource;

    @ApiModelProperty(value = "版本信息存储url")
    private String versionUrl;

    @ApiModelProperty(value = "版本信息转换")
    private Integer dataConversion;

    @TableField("deleted")
    private Boolean deleted = false;

    @ApiModelProperty(value = "资源拥有人id")
    private Long originUserId;

    public DatasetVersion() {
    }

    public DatasetVersion(String versionSource, String versionUrl, DatasetVersionCreateDTO datasetVersionCreateDTO) {
        this.datasetId = datasetVersionCreateDTO.getDatasetId();
        this.versionName = datasetVersionCreateDTO.getVersionName();
        this.versionNote = datasetVersionCreateDTO.getVersionNote();
        this.versionSource = versionSource;
        this.versionUrl = versionUrl;
        this.setCreateTime(new Timestamp(System.currentTimeMillis()));
    }

}
