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

package org.dubhe.domain.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description 返回前端请求体
 * @create 2020/4/28
 */
@Data
@ApiModel("NoteBookDTO 响应")
public class NoteBookVO implements Serializable {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("所属用户")
    private Long userId;

    @ApiModelProperty("NoteBook 名称")
    @JsonIgnore
    private String name;

    @ApiModelProperty("NoteBook 名称")
    private String noteBookName;

    @ApiModelProperty("备注描述")
    private String description;

    @ApiModelProperty("可访问jupyter地址")
    private String url;

    @JsonIgnore
    private Integer totalRunMin;

    @ApiModelProperty("CPU数量")
    private Integer cpuNum;

    @ApiModelProperty("GPU数量")
    private Integer gpuNum;

    @ApiModelProperty("内存大小（G）")
    private Integer memNum;

    @ApiModelProperty("硬盘内存大小（G）")
    private Integer diskMemNum;

    @ApiModelProperty("0运行，1停止, 2删除, 3启动中，4停止中，5删除中，6运行异常（暂未启用）")
    private Integer status;

    @ApiModelProperty("k8s响应状态码")
    private String k8sStatusCode;

    @ApiModelProperty("k8s响应状态信息")
    private String k8sStatusInfo;

    @JsonIgnore
    private String k8sNamespace;

    @JsonIgnore
    private String k8sResourceName;

    private String k8sImageName;

    @ApiModelProperty("k8s中pvc存储路径")
    private String k8sPvcPath;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private Date createTime;

    @JsonIgnore
    private Long createUserId;

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MS_PATTERN)
    private Date updateTime;

    @JsonIgnore
    private Long updateUserId;

    @ApiModelProperty("数据集名称")
    private String dataSourceName;

    @ApiModelProperty("数据集路径")
    private String dataSourcePath;

    @ApiModelProperty("算法ID")
    private Long algorithmId;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;
}
