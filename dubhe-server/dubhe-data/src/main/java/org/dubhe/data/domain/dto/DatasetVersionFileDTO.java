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
package org.dubhe.data.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description 数据集版本文件数据
 * @date 2020-07-09
 */
@Data
public class DatasetVersionFileDTO {

    private Long id;

    @ApiModelProperty("数据集ID")
    private Long datasetId;

    @ApiModelProperty("版本名称")
    private String versionName;

    @ApiModelProperty("文件ID")
    private Long fileId;

    @ApiModelProperty("文件状态 0:新增文件 1:删除文件 2:正常文件")
    private Integer status;

    @ApiModelProperty("标注状态")
    private Integer annotationStatus;

    @ApiModelProperty("备份状态，用于版本回退")
    private Integer backupStatus;

    @ApiModelProperty("更改标记，用于版本回退")
    private Integer changed;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("标签ID")
    private Long labelId;

    @ApiModelProperty("预测值")
    private Double prediction;

}
