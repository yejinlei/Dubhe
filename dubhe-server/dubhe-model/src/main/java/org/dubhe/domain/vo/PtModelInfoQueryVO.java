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
 * @description 模型管理查询返回对应信息
 * @date 2020-5-15
 */
@Data
public class PtModelInfoQueryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模型ID")
    private Long id;

    @ApiModelProperty("模型名称")
    private String name;

    @ApiModelProperty("框架类型")
    private Integer frameType;

    @ApiModelProperty("模型类型")
    private Integer modelType;

    @ApiModelProperty("模型描述")
    private String modelDescription;

    @ApiModelProperty("模型分类")
    private String modelClassName;

    @ApiModelProperty("模型地址")
    private String modelAddress;

    @ApiModelProperty("模型版本")
    private String versionNum;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("修改时间")
    private Timestamp updateTime;
}
