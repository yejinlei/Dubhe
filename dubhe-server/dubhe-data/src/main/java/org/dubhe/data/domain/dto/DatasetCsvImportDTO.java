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
 * @description 数据集CSV导入
 * @date 2021-03-24
 */
@Data
public class DatasetCsvImportDTO {

    @ApiModelProperty("数据集ID")
    private Long datasetId;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("csv文件地址: 文件地址为minio上的bucket地址")
    private String filePath;

    @ApiModelProperty("合并列")
    private int[] mergeColumn;

    @ApiModelProperty(value = "导入表格式，是否排除头")
    private Boolean excludeHeader;

}
