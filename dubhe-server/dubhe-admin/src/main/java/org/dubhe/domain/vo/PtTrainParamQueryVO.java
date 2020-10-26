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

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dubhe.base.BaseVO;

import java.io.Serializable;

/**
 * @description 任务参数查询返回查询结果
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PtTrainParamQueryVO extends BaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("任务参数ID")
    private Long id;

    @ApiModelProperty("任务参数名称")
    private String paramName;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("镜像名称")
    private String imageName;

    @ApiModelProperty(value = "镜像Project")
    private String imageNameProject;

    @ApiModelProperty(value = "镜像版本")
    private String imageTag;

    @ApiModelProperty("算法ID")
    private Long algorithmId;

    @ApiModelProperty("运行命令,输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty("算法名称")
    private String algorithmName;

    @ApiModelProperty("算法来源(1为我的算法，2为预置算法)")
    private Integer algorithmSource;

    @ApiModelProperty("数据来源路径")
    private String dataSourcePath;

    @ApiModelProperty("数据来源名称")
    private String dataSourceName;

    @ApiModelProperty("运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)")
    private JSONObject runParams;

    @ApiModelProperty("规格名称")
    private String trainJobSpecsName;

    @ApiModelProperty("类型(0为CPU，1为GPU)")
    private Integer resourcesPoolType;

    @ApiModelProperty("训练类型")
    private Integer trainType;

    @ApiModelProperty("节点个数")
    private Integer resourcesPoolNode;

    @ApiModelProperty("验证数据来源名称")
    private String valDataSourceName;

    @ApiModelProperty("验证数据来源路径")
    private String valDataSourcePath;

    @ApiModelProperty("是否验证数据集")
    private Integer valType;

    @ApiModelProperty(value = "是否打开模型选择")
    private Integer modelType;

    @ApiModelProperty(value = "模型类型(0我的模型1预置模型)")
    private Integer modelResource;

    @ApiModelProperty(value = "模型名称")
    private String modelName;

    @ApiModelProperty(value = "模型加载路径")
    private String modelLoadPathDir;

    @ApiModelProperty(value = "模型id")
    private Integer modelId;

}
