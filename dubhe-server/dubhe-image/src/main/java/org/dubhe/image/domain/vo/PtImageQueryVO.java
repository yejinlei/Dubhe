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

package org.dubhe.image.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 返回镜像查询结果
 * @date 2020-04-27
 */
@Data
public class PtImageQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("镜像ID")
    private Long id;

    @ApiModelProperty("镜像项目名")
    private String projectName;

    @ApiModelProperty("镜像名称")
    private String imageName;

    @ApiModelProperty("镜像版本")
    private String imageTag;

    @ApiModelProperty("镜像状态(0:上传中,1:上传成功,2:上传失败)")
    private Integer imageStatus;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;

    @ApiModelProperty("镜像来源")
    private Integer imageResource;
}
