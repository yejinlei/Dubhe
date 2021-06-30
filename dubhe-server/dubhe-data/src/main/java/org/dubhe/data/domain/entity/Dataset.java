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
import lombok.*;
import lombok.experimental.Accessors;
import org.dubhe.biz.base.dto.TeamDTO;
import org.dubhe.biz.base.dto.UserDTO;
import org.dubhe.biz.base.enums.DatasetTypeEnum;
import org.dubhe.data.domain.dto.DatasetCreateDTO;
import org.dubhe.data.domain.dto.DatasetCustomCreateDTO;
import org.dubhe.data.machine.constant.DataStateCodeConstant;

import java.sql.Timestamp;

/**
 * @description 数据集
 * @date 2020-04-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("data_dataset")
@ApiModel(value = "Dataset对象", description = "数据集管理")
@Builder
@ToString
@AllArgsConstructor
public class Dataset {

    private static final long serialVersionUID = 1L;

    @TableField("deleted")
    private Boolean deleted = false;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集备注
     */
    private String remark;

    @ApiModelProperty(value = "类型 0: private 私有数据,  1:team  团队数据  2:public 公开数据")
    private Integer type;

    @ApiModelProperty(value = "数据类型:0图片,1视频, 2文本")
    private Integer dataType;

    @ApiModelProperty(value = "标注类型：2分类,1目标检测,5目标跟踪，6本文分类")
    private Integer annotateType;

    private Long teamId;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "数据集存储位置")
    private String uri;

    @ApiModelProperty(value = "0:未标注，1:手动标注中，2:自动标注中，3:自动标注完成，4:标注完成")
    private Integer status;

    @ApiModelProperty(value = "当前版本号")
    private String currentVersionName;

    @ApiModelProperty(value = "是否用户导入")
    @TableField(value = "is_import")
    private boolean isImport;

    @ApiModelProperty(value = "用户导入数据集压缩包地址")
    private String archiveUrl;

    @ApiModelProperty(value = "解压状态: 0未解压 1解压中 2解压完成 3解压失败")
    private Integer decompressState;

    @ApiModelProperty(value = "解压失败原因")
    private String decompressFailReason;

    @ApiModelProperty(value = "是否置顶")
    @TableField(value = "is_top")
    private boolean isTop;

    @ApiModelProperty(value = "标签组Id")
    private Long labelGroupId;

    @ApiModelProperty(value = "数据集源ID")
    private Long sourceId;

    /**
     * 团队
     */
    @TableField(exist = false)
    private TeamDTO team;

    /**
     * 创建用户
     */
    @TableField(exist = false)
    private UserDTO createUser;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Timestamp createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Timestamp updateTime;

    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    private Long originUserId;

    @TableField(exist = false)
    private UserDTO updateUser;

    public Dataset() {
    }

    public Dataset(Long id, Integer status) {
        this.id = id;
        this.status = status;
    }

    public Dataset(DatasetCreateDTO datasetCreateDTO) {
        this.name = datasetCreateDTO.getName();
        this.remark = datasetCreateDTO.getRemark();
        this.type = datasetCreateDTO.getType();
        this.teamId = datasetCreateDTO.getTeamId();
        this.dataType = datasetCreateDTO.getDataType();
        this.annotateType = datasetCreateDTO.getAnnotateType();
        this.isImport = datasetCreateDTO.isImport();
        this.labelGroupId = datasetCreateDTO.getLabelGroupId();
    }

    /**
     * 用户自定义数据集
     * @param datasetCustomCreateDTO
     */
    public Dataset(DatasetCustomCreateDTO datasetCustomCreateDTO) {
        this.name = datasetCustomCreateDTO.getName();
        this.remark = datasetCustomCreateDTO.getDesc();
        this.type = DatasetTypeEnum.PRIVATE.getValue();
        this.dataType = datasetCustomCreateDTO.getDatasetType();
        this.annotateType = datasetCustomCreateDTO.getAnnotateType();
        this.status = DataStateCodeConstant.ANNOTATION_COMPLETE_STATE;
        this.archiveUrl = datasetCustomCreateDTO.getArchiveUrl();
        this.isImport = true;
    }

}
