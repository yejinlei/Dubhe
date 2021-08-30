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

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dubhe.biz.base.constant.NumberConstant;
import org.dubhe.biz.base.constant.UserConstant;
import org.dubhe.biz.db.entity.BaseEntity;
import org.dubhe.data.domain.dto.DatasetVersionCreateDTO;

import java.sql.Timestamp;

/**
 * @description 数据集版本管理
 * @date 2020-05-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("data_dataset_version")
@ApiModel(value = "Dataset版本对象", description = "数据集版本管理")
@Builder
@AllArgsConstructor
public class DatasetVersion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "所属数据集ID")
    private Long datasetId;

    @ApiModelProperty(value = "团队ID")
    private Long teamId;


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

    @TableField(value = "deleted",fill = FieldFill.INSERT)
    private Boolean deleted = false;

    @ApiModelProperty(value = "资源拥有人id")
    private Long originUserId;

    @ApiModelProperty(value = "是否生成ofRecord文件")
    private Integer ofRecord;

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

    public DatasetVersion(Long datasetId, String versionName, String versionNote) {
        this.datasetId = datasetId;
        this.versionName = versionName;
        this.setCreateUserId(UserConstant.DEFAULT_CREATE_USER_ID);
        this.setCreateTime(new Timestamp(System.currentTimeMillis()));
        this.versionUrl = "dataset/"+datasetId +"/versionFile/"+versionName;
        this.dataConversion = NumberConstant.NUMBER_2;
        this.originUserId = UserConstant.DEFAULT_ORIGIN_USER_ID;
        this.versionNote = versionNote;
    }

}
