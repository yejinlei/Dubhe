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

package org.dubhe.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 版本管理删除返回对应id
 * @date 2020-5-15
 */
@Data
public class PtModelBranchQueryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "版本ID")
    private Long id;

    @ApiModelProperty(value = "父ID")
    private Long parentId;

    @ApiModelProperty(value = "版本号")
    private String versionNum;

    @ApiModelProperty(value = "模型地址")
    private String modelAddress;

    @ApiModelProperty(value = "模型路径")
    private String modelPath;

    @ApiModelProperty("模型来源(0用户上传，1训练输出，2模型优化)")
    private Integer modelSource;

    @ApiModelProperty(value = "算法ID")
    private Long algorithmId;

    @ApiModelProperty(value = "算法名称")
    private String algorithmName;

    @ApiModelProperty(value = "算法来源(1为我的算法，2为预置算法)")
    private Integer algorithmSource;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty("修改时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
