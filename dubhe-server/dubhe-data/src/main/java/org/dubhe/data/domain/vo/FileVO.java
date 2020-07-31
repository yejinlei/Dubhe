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

package org.dubhe.data.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description 文件信息
 * @date 2020-04-17
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "File vo", description = "文件信息")
public class FileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    @ApiModelProperty(value = "状态:0-未标注，1-标注中，2-自动标注完成，3-已标注完成")
    private Integer status;

    @ApiModelProperty(value = "数据集id")
    private Long datasetId;

    @ApiModelProperty(value = "资源访问路径")
    private String url;

    @ApiModelProperty(value = "更新用户ID")
    private Long updateUserId;

    private String annotation;

    @ApiModelProperty(value = "父文件id")
    private Long pid;

    @ApiModelProperty(value = "增强类型")
    private Integer enhanceType;

}
