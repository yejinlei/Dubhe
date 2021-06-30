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
package org.dubhe.measure.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * @description 返回度量查询结果
 * @date 2020-11-16
 */
@Data
public class PtMeasureQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("度量名称")
    private String name;

    @ApiModelProperty("度量文件url")
    private String url;

    @ApiModelProperty("度量描述")
    private String description;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;

    @ApiModelProperty("数据集Id")
    private Long datasetId;

    @ApiModelProperty("数据集url")
    private String datasetUrl;

    @ApiModelProperty("模型url")
    private List<String> modelUrls;

    @ApiModelProperty("度量文件生成状态")
    private Integer measureStatus;
}
