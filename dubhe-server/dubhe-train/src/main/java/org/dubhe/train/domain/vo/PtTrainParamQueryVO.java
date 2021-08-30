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

package org.dubhe.train.domain.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dubhe.biz.base.vo.BaseVO;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 任务参数查询返回查询结果
 * @date 2020-04-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PtTrainParamQueryVO extends BaseVO implements Serializable {

    @ApiModelProperty("任务参数ID")
    private Long id;

    @ApiModelProperty("任务参数名称")
    private String paramName;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("镜像名称")
    private String imageName;

    @ApiModelProperty("镜像Project")
    private String imageNameProject;

    @ApiModelProperty("镜像版本")
    private String imageTag;

    @ApiModelProperty("算法ID")
    private Long algorithmId;

    @ApiModelProperty("运行命令,输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty("算法名称")
    private String algorithmName;

    @ApiModelProperty("算法来源(1为我的算法，2为预置算法)")
    private Integer algorithmSource;

    @ApiModelProperty("算法用途")
    private String algorithmUsage;

    @ApiModelProperty("验证数据集算法用途")
    private String valAlgorithmUsage;

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

    @ApiModelProperty("模型类型(0我的模型1预置模型2炼知模型)")
    private Integer modelResource;

    @ApiModelProperty("模型id")
    private Long modelId;

    @ApiModelProperty("我的模型版本对应的id")
    private Long modelBranchId;

    @ApiModelProperty("炼知教师模型ids,多个id之前用','隔开")
    private String teacherModelIds;

    @ApiModelProperty("炼知学生模型ids,多个id之前用','隔开")
    private String studentModelIds;

    @ApiModelProperty("创建人")
    private Long createUserId;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新人")
    private Long updateUserId;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;

    @ApiModelProperty("资源拥有者ID")
    private Long originUserId;

}
