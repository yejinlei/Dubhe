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

package org.dubhe.data.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description 文本文件信息
 * @date 2021-01-10
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "TxtFile vo", description = "文本文件信息")
public class TxtFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文本文件id")
    private Long id;

    @ApiModelProperty(value = "文本文件名称")
    private String name;

    @ApiModelProperty(value = "状态: 101-未标注, 102-手动标注中, 103-自动标注完成, 104-标注完成, 105-标注未识别")
    private Integer status;

    @ApiModelProperty(value = "数据集id")
    private Long datasetId;

    @ApiModelProperty(value = "资源访问路径")
    private String url;

    @ApiModelProperty("预测值")
    private Double prediction;

    @ApiModelProperty("标注ID")
    private Long labelId;

    @ApiModelProperty("摘要名称")
    private String abstractName;

    @ApiModelProperty("摘要Url")
    private String abstractUrl;

    @ApiModelProperty("文本内容")
    private String content;

    @ApiModelProperty("标注信息")
    private String annotation;
}
