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
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @description 返回训练版本查询详情
 * @date 2020-04-27
 */
@Data
@Accessors(chain = true)
public class PtTrainJobDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("训练作业名")
    private String trainName;

    @ApiModelProperty("jobID")
    private Long id;

    @ApiModelProperty("训练作业ID")
    private Long trainId;

    @ApiModelProperty("训练作业job版本")
    private String trainVersion;

    @ApiModelProperty("训练作业job父版本")
    private String parentTrainVersion;

    @ApiModelProperty("训练作业jobName")
    private String jobName;

    @ApiModelProperty("描述信息")
    private String description;

    @ApiModelProperty("数据集名称")
    private String dataSourceName;

    @ApiModelProperty("数据集路径")
    private String dataSourcePath;

    @ApiModelProperty("训练时长")
    private String runtime;

    @ApiModelProperty("训练模型输出位置")
    private String modelPath;

    @ApiModelProperty("训练输出路径")
    private String outPath;

    @ApiModelProperty("可视化日志路径")
    private String visualizedLogPath;

    @ApiModelProperty("规格名称")
    private String trainJobSpecsName;

    @ApiModelProperty("类型(0为CPU，1为GPU)")
    private Integer resourcesPoolType;

    @ApiModelProperty("节点个数")
    private Integer resourcesPoolNode;

    @ApiModelProperty("训练作业job状态, 0为待处理，1为运行中，2为运行完成，3为失败，4为停止，5为未知，6为删除，7为创建失败")
    private Integer trainStatus;

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

    @ApiModelProperty("算法ID")
    private Long algorithmId;

    @ApiModelProperty("镜像名称")
    private String imageName;

    @ApiModelProperty(value = "镜像Project")
    private String imageNameProject;

    @ApiModelProperty(value = "镜像版本")
    private String imageTag;

    @ApiModelProperty("运行命令,输入长度不能超过128个字符")
    private String runCommand;

    @ApiModelProperty("运行参数(算法来源为我的算法时为调优参数，算法来源为预置算法时为运行参数)")
    private JSONObject runParams;

    @ApiModelProperty("F1值")
    private String paramF1;

    @ApiModelProperty("召回率")
    private String paramCallback;

    @ApiModelProperty("精确率")
    private String paramPrecise;

    @ApiModelProperty("准确率")
    private String paramAccuracy;

    @ApiModelProperty("算法名称")
    private String algorithmName;

    @ApiModelProperty("算法来源(1为我的算法，2为预置算法)")
    private Integer algorithmSource;

    @ApiModelProperty("算法用途")
    private String algorithmUsage;

    @ApiModelProperty("验证数据集算法用途")
    private String valAlgorithmUsage;

    @ApiModelProperty("算法精度")
    private String accuracy;

    @ApiModelProperty("P4推理速度（ms）")
    private Integer p4InferenceSpeed;

    @ApiModelProperty(value = "算法文件路径")
    private String algorithmCodeDir;

    @ApiModelProperty("训练类型 0：普通训练，1：分布式训练")
    private Integer trainType;

    @ApiModelProperty("验证数据来源名称")
    private String valDataSourceName;

    @ApiModelProperty("验证数据来源路径")
    private String valDataSourcePath;

    @ApiModelProperty("是否验证数据集")
    private Integer valType;

    @ApiModelProperty("训练延时启动倒计时，单位：分钟")
    private Integer delayCreateCountDown;

    @ApiModelProperty("训练自动停止倒计时，单位：分钟")
    private Integer delayDeleteCountDown;

    @ApiModelProperty(value = "模型类型(0我的模型1预置模型2炼知模型)")
    private Integer modelResource;

    @ApiModelProperty(value = "非炼知模型id")
    private Long modelId;

    @ApiModelProperty(value = "我的模型版本对应的id")
    private Long modelBranchId;

    @ApiModelProperty(value = "训练信息(失败信息)")
    private String trainMsg;

    @ApiModelProperty("训练状态信息")
    private String statusDetail;

    @ApiModelProperty(value = "炼知教师模型ids,多个id之前用','隔开")
    private String teacherModelIds;

    @ApiModelProperty(value = "炼知学生模型ids,多个id之前用','隔开")
    private String studentModelIds;

    @ApiModelProperty(value = "notebook名称")
    private String notebookName;

    @ApiModelProperty(value = "notebookId")
    private Long notebookId;
}