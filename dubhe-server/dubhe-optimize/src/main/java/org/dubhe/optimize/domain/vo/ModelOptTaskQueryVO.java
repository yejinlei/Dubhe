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

package org.dubhe.optimize.domain.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.dubhe.biz.base.vo.BaseVO;

/**
 * @description 查询模型优化任务返回结果
 * @date 2020-05-22
 */
@Data
public class ModelOptTaskQueryVO extends BaseVO {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("任务名称")
    private String name;

    @ApiModelProperty("任务描述")
    private String description;

    @ApiModelProperty(value = "数据集id")
    private Long datasetId;

    @ApiModelProperty("数据集名称")
    private String datasetName;

    @ApiModelProperty("数据集路径")
    private String datasetPath;

    @ApiModelProperty("是否内置")
    private Boolean isBuiltIn;

    @ApiModelProperty("模型id")
    private Long modelId;

    @ApiModelProperty("模型名称")
    private String modelName;

    @ApiModelProperty("模型路径")
    private String modelAddress;

    @ApiModelProperty("优化算法类型")
    private Integer algorithmType;

    @ApiModelProperty("优化算法")
    private String algorithmName;

    @ApiModelProperty("优化算法id")
    private Long algorithmId;

    @ApiModelProperty("优化算法路径")
    private String algorithmPath;

    @ApiModelProperty(value = "运行命令")
    private String command;

    @ApiModelProperty(value = "运行参数")
    private JSONObject params;
}
