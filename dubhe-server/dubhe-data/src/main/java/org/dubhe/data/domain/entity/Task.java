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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @description 标注任务信息
 * @date 2020-04-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("data_task")
@ApiModel(value = "Task对象", description = "标注任务信息")
@Getter
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "任务需要处理的文件总数")
    private Integer total;

    @ApiModelProperty(value = "任务状态，创建即为进行中。1进行中，2已完成")
    private Integer status;

    @ApiModelProperty(value = "已完成的文件数")
    private Integer finished;

    @ApiModelProperty(value = "失败的文件数")
    private Integer failed;

    @ApiModelProperty(value = "文件id数组")
    private String files;

    @ApiModelProperty(value = "数据集ID")
    private Long datasetId;

    @ApiModelProperty(value = "数据集id数组")
    private String datasets;

    @ApiModelProperty(value = "创建用户ID")
    private Long createUserId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "0正常，1已删除")
    private Boolean deleted;

    @ApiModelProperty(value = "同dataset")
    private Integer annotateType;

    @ApiModelProperty(value = "同dataset")
    private Integer dataType;

    private String labels;

    @ApiModelProperty(value = "任务类型 0.自动标注 1.ofrecord 2.imageNet 3.数据增强 4.目标跟踪 5.视频采样 6.文本分类")
    private Integer type;

    @ApiModelProperty(value = "数据集版本ID")
    private Long datasetVersionId;

    @ApiModelProperty(value = "增强类型")
    private String enhanceType;

    @ApiModelProperty(value = "地址")
    private String url;

    @ApiModelProperty(value = "帧间隔")
    private Integer frameInterval;

    @ApiModelProperty(value = "需要合并的列")
    private String mergeColumn;

    @ApiModelProperty(value = "版本名称")
    private String versionName;

    @ApiModelProperty(value = "目的ID")
    private Long targetId;

}
